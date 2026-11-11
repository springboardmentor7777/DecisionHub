package com.decisionhub.controller;

import com.decisionhub.dto.ApiDtos.*;
import com.decisionhub.service.BoardService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/boards")
public class BoardController {
    private final BoardService boards;

    public BoardController(BoardService boards) {
        this.boards = boards;
    }

    @GetMapping("/public")
    public List<BoardResponse> publicBoards() {
        return boards.publicBoards();
    }

    @GetMapping("/mine")
    public List<BoardResponse> myBoards(@AuthenticationPrincipal UserDetails user) {
        return boards.myBoards(user.getUsername());
    }

    @GetMapping("/{id}")
    public BoardResponse get(@PathVariable Long id, @AuthenticationPrincipal UserDetails user) {
        return boards.get(id, user == null ? null : user.getUsername());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BoardResponse create(@AuthenticationPrincipal UserDetails user,
                                @Valid @RequestBody CreateBoardRequest request) {
        return boards.create(user.getUsername(), request);
    }

    @PutMapping("/{id}")
    public BoardResponse update(@PathVariable Long id, @AuthenticationPrincipal UserDetails user,
                                @Valid @RequestBody UpdateBoardRequest request) {
        return boards.update(id, user.getUsername(), request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id, @AuthenticationPrincipal UserDetails user) {
        boards.delete(id, user.getUsername());
    }

    @PostMapping("/{id}/options")
    public BoardResponse addOption(@PathVariable Long id, @AuthenticationPrincipal UserDetails user,
                                   @Valid @RequestBody OptionRequest request) {
        return boards.addOption(id, user.getUsername(), request);
    }
}
