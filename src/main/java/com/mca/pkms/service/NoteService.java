package com.mca.pkms.service;

import com.mca.pkms.dto.AutoSaveRequest;
import com.mca.pkms.dto.DashboardStats;
import com.mca.pkms.dto.NoteForm;
import com.mca.pkms.entity.*;
import com.mca.pkms.exception.ResourceNotFoundException;
import com.mca.pkms.repository.CategoryRepository;
import com.mca.pkms.repository.FavoriteRepository;
import com.mca.pkms.repository.NoteRepository;
import com.mca.pkms.repository.TagRepository;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class NoteService {
    static {
        System.setProperty("pdfbox.fontcache",
                System.getProperty("pdfbox.fontcache", System.getProperty("java.io.tmpdir")));
    }

    private final NoteRepository noteRepository;
    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;
    private final FavoriteRepository favoriteRepository;

    public NoteService(NoteRepository noteRepository, CategoryRepository categoryRepository,
                       TagRepository tagRepository, FavoriteRepository favoriteRepository) {
        this.noteRepository = noteRepository;
        this.categoryRepository = categoryRepository;
        this.tagRepository = tagRepository;
        this.favoriteRepository = favoriteRepository;
    }

    public DashboardStats stats(User user) {
        return new DashboardStats(
                noteRepository.countByUserAndDeletedFalse(user),
                noteRepository.countByUserAndDeletedFalseAndArchivedFalse(user),
                noteRepository.countByUserAndDeletedFalseAndArchivedTrue(user),
                noteRepository.countByUserAndDeletedTrue(user),
                favoriteRepository.findByUserOrderByCreatedAtDesc(user).stream()
                        .filter(f -> !f.getNote().isDeleted()).count());
    }

    public List<Note> dashboardNotes(User user) {
        return noteRepository.findByUserAndDeletedFalseAndArchivedFalseOrderByUpdatedAtDesc(user);
    }

    public List<Note> archived(User user) {
        return noteRepository.findByUserAndDeletedFalseAndArchivedTrueOrderByUpdatedAtDesc(user);
    }

    public List<Note> trash(User user) {
        return noteRepository.findByUserAndDeletedTrueOrderByDeletedAtDesc(user);
    }

    public List<Note> favorites(User user) {
        return favoriteRepository.findByUserOrderByCreatedAtDesc(user).stream()
                .map(Favorite::getNote)
                .filter(note -> !note.isDeleted())
                .toList();
    }

    public Note find(Long id, User user) {
        return noteRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Note not found."));
    }

    public boolean isFavorite(Long noteId, User user) {
        return favoriteRepository.existsByUser_IdAndNote_Id(user.getId(), noteId);
    }

    @Transactional
    public Note create(NoteForm form, User user) {
        Note note = new Note();
        apply(note, form.getTitle(), form.getContent(), form.getCategoryId(), form.getTagIds(), user);
        note.setArchived(form.isArchived());
        return noteRepository.save(note);
    }

    @Transactional
    public Note update(Long id, NoteForm form, User user) {
        Note note = find(id, user);
        apply(note, form.getTitle(), form.getContent(), form.getCategoryId(), form.getTagIds(), user);
        note.setArchived(form.isArchived());
        note.setAutoSaved(false);
        return noteRepository.save(note);
    }

    @Transactional
    public Note autoSave(Long id, AutoSaveRequest request, User user) {
        Note note = id == null ? new Note() : find(id, user);
        String title = Optional.ofNullable(request.getTitle()).filter(s -> !s.isBlank()).orElse("Untitled note");
        String content = Optional.ofNullable(request.getContent()).filter(s -> !s.isBlank()).orElse("<p></p>");
        apply(note, title, content, request.getCategoryId(), request.getTagIds(), user);
        note.setAutoSaved(true);
        return noteRepository.save(note);
    }

    @Transactional
    public void softDelete(Long id, User user) {
        Note note = find(id, user);
        note.softDelete();
        noteRepository.save(note);
    }

    @Transactional
    public void restore(Long id, User user) {
        Note note = find(id, user);
        note.restore();
        noteRepository.save(note);
    }

    @Transactional
    public void deleteForever(Long id, User user) {
        Note note = find(id, user);
        favoriteRepository.deleteByNote_Id(note.getId());
        noteRepository.delete(note);
    }

    @Transactional
    public void toggleArchive(Long id, User user) {
        Note note = find(id, user);
        note.setArchived(!note.isArchived());
        noteRepository.save(note);
    }

    @Transactional
    public void toggleFavorite(Long id, User user) {
        Note note = find(id, user);
        UserFavoriteId favoriteId = new UserFavoriteId(user.getId(), note.getId());
        if (favoriteRepository.existsById(favoriteId)) {
            favoriteRepository.deleteById(favoriteId);
            return;
        }
        favoriteRepository.save(new Favorite(user, note));
    }

    public List<Note> search(User user, String query, Long categoryId, Set<Long> tagIds) {
        boolean tagIdsEmpty = tagIds == null || tagIds.isEmpty();
        Collection<Long> filterTags = tagIdsEmpty ? List.of(-1L) : tagIds;
        List<Note> notes = noteRepository.filter(user, false, categoryId, filterTags, tagIdsEmpty);
        if (query == null || query.isBlank()) {
            return notes.stream().sorted(Comparator.comparing(Note::getUpdatedAt).reversed()).toList();
        }
        List<String> keywords = tokenize(query);
        return notes.stream()
                .map(note -> Map.entry(note, relevance(note, keywords)))
                .filter(entry -> entry.getValue() > 0)
                .sorted(Map.Entry.<Note, Integer>comparingByValue().reversed()
                        .thenComparing(entry -> entry.getKey().getUpdatedAt(), Comparator.reverseOrder()))
                .map(Map.Entry::getKey)
                .toList();
    }

    public byte[] exportTxt(Long id, User user) {
        Note note = find(id, user);
        String text = note.getTitle() + System.lineSeparator() + System.lineSeparator() + note.getPlainText();
        return text.getBytes(StandardCharsets.UTF_8);
    }

    public byte[] exportPdf(Long id, User user) {
        Note note = find(id, user);
        try (PDDocument document = new PDDocument(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);
            float margin = 56;
            float y = page.getMediaBox().getHeight() - margin;
            PDPageContentStream contentStream = new PDPageContentStream(document, page);

            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 18);
            contentStream.newLineAtOffset(margin, y);
            y = writeWrappedText(contentStream, note.getTitle(), 18, y, 24, 58);
            contentStream.setFont(PDType1Font.HELVETICA, 11);
            y -= 12;

            String body = Optional.ofNullable(note.getPlainText()).orElse("").replaceAll("\\s+", " ").trim();
            for (String paragraph : body.split("(?<=\\.)\\s+")) {
                if (y < margin + 24) {
                    contentStream.endText();
                    contentStream.close();
                    page = new PDPage(PDRectangle.A4);
                    document.addPage(page);
                    contentStream = new PDPageContentStream(document, page);
                    contentStream.beginText();
                    contentStream.setFont(PDType1Font.HELVETICA, 11);
                    y = page.getMediaBox().getHeight() - margin;
                    contentStream.newLineAtOffset(margin, y);
                }
                y = writeWrappedText(contentStream, paragraph, 11, y, 15, 92);
                y -= 6;
            }

            contentStream.endText();
            contentStream.close();
            document.save(outputStream);
            return outputStream.toByteArray();
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to export PDF.", ex);
        }
    }

    private void apply(Note note, String title, String content, Long categoryId, Set<Long> tagIds, User user) {
        note.setTitle(title.trim());
        note.setContent(sanitize(content));
        note.setPlainText(Jsoup.parse(content).text());
        note.setUser(user);
        note.setCategory(categoryId == null ? null : categoryRepository.findByIdAndUser(categoryId, user).orElse(null));
        note.setTags(tagIds == null || tagIds.isEmpty() ? new HashSet<>() : tagRepository.findByIdInAndUser(tagIds, user));
    }

    private String sanitize(String html) {
        Safelist safelist = Safelist.relaxed()
                .addTags("span")
                .addAttributes("span", "style")
                .addAttributes(":all", "class");
        return Jsoup.clean(html, safelist);
    }

    private List<String> tokenize(String query) {
        return Arrays.stream(query.toLowerCase(Locale.ROOT).split("[^a-z0-9]+"))
                .filter(token -> token.length() > 1)
                .distinct()
                .toList();
    }

    private int relevance(Note note, List<String> keywords) {
        String title = note.getTitle().toLowerCase(Locale.ROOT);
        String content = Optional.ofNullable(note.getPlainText()).orElse("").toLowerCase(Locale.ROOT);
        int score = 0;
        for (String keyword : keywords) {
            if (title.equals(keyword)) score += 20;
            if (title.contains(keyword)) score += 10;
            if (content.contains(keyword)) score += 3;
            for (Tag tag : note.getTags()) {
                if (tag.getName().toLowerCase(Locale.ROOT).contains(keyword)) score += 5;
            }
        }
        return score;
    }

    private float writeWrappedText(PDPageContentStream contentStream, String text, int fontSize,
                                   float y, float lineHeight, int maxChars) throws IOException {
        String remaining = Optional.ofNullable(text).orElse("").trim();
        if (remaining.isBlank()) {
            contentStream.newLineAtOffset(0, -lineHeight);
            return y - lineHeight;
        }
        while (!remaining.isBlank()) {
            int length = Math.min(maxChars, remaining.length());
            int breakAt = remaining.length() <= maxChars ? remaining.length() : remaining.lastIndexOf(' ', length);
            if (breakAt <= 0) {
                breakAt = length;
            }
            String line = remaining.substring(0, breakAt).trim();
            contentStream.showText(line);
            contentStream.newLineAtOffset(0, -lineHeight);
            y -= lineHeight;
            remaining = remaining.substring(breakAt).trim();
        }
        return y;
    }
}
