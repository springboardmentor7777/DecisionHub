package com.decisionhub.controller;

import com.decisionhub.dto.ApiDtos.*;
import com.decisionhub.entity.BoardVisibility;
import com.decisionhub.entity.PollType;
import com.decisionhub.service.BoardService;
import jakarta.validation.Valid;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
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

    /** Optional filters: category (case-insensitive), q (title, description or option name), pollType. */
    @GetMapping("/public")
    public List<BoardResponse> publicBoards(@RequestParam(required = false) String category,
                                            @RequestParam(required = false) String q,
                                            @RequestParam(required = false) String pollType) {
        return boards.publicBoards(category, q, parseEnum(PollType.class, pollType, "pollType"));
    }

    /** Same filters as /public, plus visibility (PUBLIC or PRIVATE). */
    @GetMapping("/mine")
    public List<BoardResponse> myBoards(@AuthenticationPrincipal UserDetails user,
                                        @RequestParam(required = false) String category,
                                        @RequestParam(required = false) String q,
                                        @RequestParam(required = false) String pollType,
                                        @RequestParam(required = false) String visibility) {
        return boards.myBoards(user.getUsername(), category, q,
                parseEnum(PollType.class, pollType, "pollType"),
                parseEnum(BoardVisibility.class, visibility, "visibility"));
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

    @GetMapping("/{id}/options")
    public List<OptionResponse> options(@PathVariable Long id, @AuthenticationPrincipal UserDetails user) {
        return boards.options(id, user == null ? null : user.getUsername());
    }

    @PostMapping("/{id}/options")
    public BoardResponse addOption(@PathVariable Long id, @AuthenticationPrincipal UserDetails user,
                                   @Valid @RequestBody OptionRequest request) {
        return boards.addOption(id, user.getUsername(), request);
    }

    @PutMapping("/{id}/options/{optionId}")
    public BoardResponse updateOption(@PathVariable Long id, @PathVariable Long optionId,
                                      @AuthenticationPrincipal UserDetails user,
                                      @Valid @RequestBody OptionRequest request) {
        return boards.updateOption(id, optionId, user.getUsername(), request);
    }

    @DeleteMapping("/{id}/options/{optionId}")
    public BoardResponse deleteOption(@PathVariable Long id, @PathVariable Long optionId,
                                      @AuthenticationPrincipal UserDetails user) {
        return boards.deleteOption(id, optionId, user.getUsername());
    }

    private static <E extends Enum<E>> E parseEnum(Class<E> type, String value, String name) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Enum.valueOf(type, value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(name + " must be one of " + Arrays.toString(type.getEnumConstants()));
        }
    }
}
