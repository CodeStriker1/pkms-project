package com.mca.pkms.controller;

import com.mca.pkms.dto.AutoSaveRequest;
import com.mca.pkms.dto.NoteForm;
import com.mca.pkms.entity.Note;
import com.mca.pkms.entity.User;
import com.mca.pkms.service.CategoryService;
import com.mca.pkms.service.NoteService;
import com.mca.pkms.service.TagService;
import com.mca.pkms.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;

@Controller
@RequestMapping("/notes")
public class NoteController extends BaseController {
    private final NoteService noteService;
    private final CategoryService categoryService;
    private final TagService tagService;

    public NoteController(UserService userService, NoteService noteService, CategoryService categoryService, TagService tagService) {
        super(userService);
        this.noteService = noteService;
        this.categoryService = categoryService;
        this.tagService = tagService;
    }

    @GetMapping
    public String list(@RequestParam(required = false) String q,
                       @RequestParam(required = false) Long categoryId,
                       @RequestParam(required = false) Set<Long> tagIds,
                       Authentication authentication, Model model) {
        User user = currentUser(authentication);
        model.addAttribute("notes", noteService.search(user, q, categoryId, tagIds));
        model.addAttribute("categories", categoryService.list(user));
        model.addAttribute("tags", tagService.list(user));
        model.addAttribute("q", q);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("selectedTagIds", tagIds == null ? Set.of() : tagIds);
        return "notes/list";
    }

    @GetMapping("/new")
    public String createForm(Authentication authentication, Model model) {
        model.addAttribute("noteForm", new NoteForm());
        addLookups(authentication, model);
        return "notes/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute NoteForm noteForm, BindingResult bindingResult,
                         Authentication authentication, Model model, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            addLookups(authentication, model);
            return "notes/form";
        }
        Note note = noteService.create(noteForm, currentUser(authentication));
        redirectAttributes.addFlashAttribute("success", "Note created.");
        return "redirect:/notes/" + note.getId();
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Authentication authentication, Model model) {
        User user = currentUser(authentication);
        model.addAttribute("note", noteService.find(id, user));
        model.addAttribute("favorite", noteService.isFavorite(id, user));
        return "notes/detail";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Authentication authentication, Model model) {
        User user = currentUser(authentication);
        Note note = noteService.find(id, user);
        NoteForm form = new NoteForm();
        form.setTitle(note.getTitle());
        form.setContent(note.getContent());
        form.setArchived(note.isArchived());
        form.setCategoryId(note.getCategory() == null ? null : note.getCategory().getId());
        form.setTagIds(note.getTags().stream().map(tag -> tag.getId()).collect(java.util.stream.Collectors.toSet()));
        model.addAttribute("note", note);
        model.addAttribute("noteForm", form);
        addLookups(authentication, model);
        return "notes/form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id, @Valid @ModelAttribute NoteForm noteForm, BindingResult bindingResult,
                         Authentication authentication, Model model, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("note", noteService.find(id, currentUser(authentication)));
            addLookups(authentication, model);
            return "notes/form";
        }
        noteService.update(id, noteForm, currentUser(authentication));
        redirectAttributes.addFlashAttribute("success", "Note updated.");
        return "redirect:/notes/" + id;
    }

    @PostMapping("/{id}/autosave")
    @ResponseBody
    public Map<String, Object> autoSave(@PathVariable Long id, @RequestBody AutoSaveRequest request,
                                        Authentication authentication) {
        Note note = noteService.autoSave(id, request, currentUser(authentication));
        return Map.of("id", note.getId(), "saved", true, "updatedAt", note.getUpdatedAt().toString());
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, Authentication authentication, RedirectAttributes redirectAttributes) {
        noteService.softDelete(id, currentUser(authentication));
        redirectAttributes.addFlashAttribute("success", "Note moved to trash.");
        return "redirect:/notes";
    }

    @PostMapping("/{id}/restore")
    public String restore(@PathVariable Long id, Authentication authentication, RedirectAttributes redirectAttributes) {
        noteService.restore(id, currentUser(authentication));
        redirectAttributes.addFlashAttribute("success", "Note restored.");
        return "redirect:/trash";
    }

    @PostMapping("/{id}/delete-forever")
    public String deleteForever(@PathVariable Long id, Authentication authentication, RedirectAttributes redirectAttributes) {
        noteService.deleteForever(id, currentUser(authentication));
        redirectAttributes.addFlashAttribute("success", "Note deleted permanently.");
        return "redirect:/trash";
    }

    @PostMapping("/{id}/archive")
    public String archive(@PathVariable Long id, Authentication authentication) {
        noteService.toggleArchive(id, currentUser(authentication));
        return "redirect:/notes/" + id;
    }

    @PostMapping("/{id}/favorite")
    public String favorite(@PathVariable Long id, Authentication authentication) {
        noteService.toggleFavorite(id, currentUser(authentication));
        return "redirect:/notes/" + id;
    }

    @GetMapping("/{id}/export/txt")
    public ResponseEntity<byte[]> exportTxt(@PathVariable Long id, Authentication authentication) {
        byte[] body = noteService.exportTxt(id, currentUser(authentication));
        return ResponseEntity.ok()
                .contentType(new MediaType("text", "plain", StandardCharsets.UTF_8))
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename("note-" + id + ".txt").build().toString())
                .body(body);
    }

    @GetMapping("/{id}/export/pdf")
    public ResponseEntity<byte[]> exportPdf(@PathVariable Long id, Authentication authentication) {
        byte[] body = noteService.exportPdf(id, currentUser(authentication));
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename("note-" + id + ".pdf").build().toString())
                .body(body);
    }

    private void addLookups(Authentication authentication, Model model) {
        User user = currentUser(authentication);
        model.addAttribute("categories", categoryService.list(user));
        model.addAttribute("tags", tagService.list(user));
    }
}
