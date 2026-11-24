package com.decisionhub.controller;

import com.decisionhub.dto.DecisionDtos.*;
import com.decisionhub.service.DecisionBoardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.constraints.Size;

import java.util.List;

@RestController
@RequestMapping("/api/decisions")
@RequiredArgsConstructor
public class DecisionBoardController {
    private final DecisionBoardService boardService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BoardResponse create(Authentication auth, @Valid @RequestBody CreateBoardRequest request) {
        return boardService.create(auth.getName(), request);
    }

    @GetMapping
    public List<BoardResponse> list(Authentication auth, @RequestParam(required = false) String category) {
        return boardService.list(auth.getName(), category);
    }

    @GetMapping("/{id}")
    public BoardResponse get(Authentication auth, @PathVariable Long id) {
        return boardService.get(auth.getName(), id);
    }

    @PutMapping("/{id}")
    public BoardResponse update(Authentication auth, @PathVariable Long id,
                                @Valid @RequestBody UpdateBoardRequest request) {
        return boardService.update(auth.getName(), id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(Authentication auth, @PathVariable Long id) {
        boardService.delete(auth.getName(), id);
    }

    @PostMapping("/{id}/options")
    @ResponseStatus(HttpStatus.CREATED)
    public OptionResponse addOption(Authentication auth, @PathVariable Long id,
                                    @Valid @RequestBody OptionRequest request) {
        return boardService.addOption(auth.getName(), id, request);
    }

    @PutMapping("/{id}/options/{optionId}")
    public OptionResponse updateOption(Authentication auth, @PathVariable Long id,
                                       @PathVariable Long optionId,
                                       @Valid @RequestBody OptionRequest request) {
        return boardService.updateOption(auth.getName(), id, optionId, request);
    }

    @DeleteMapping("/{id}/options/{optionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteOption(Authentication auth, @PathVariable Long id, @PathVariable Long optionId) {
        boardService.deleteOption(auth.getName(), id, optionId);
    }

    @PutMapping("/{id}/factors")
    public List<FactorResponse> replaceFactors(Authentication auth, @PathVariable Long id,
                                               @RequestBody @Size(max = 50)
                                               List<@Valid FactorRequest> factors) {
        return boardService.replaceFactors(auth.getName(), id, factors);
    }

    @GetMapping("/{id}/factors")
    public List<FactorResponse> listFactors(Authentication auth, @PathVariable Long id) {
        return boardService.listFactors(auth.getName(), id);
    }

    @PutMapping("/{id}/options/{optionId}/scores")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void setScores(Authentication auth, @PathVariable Long id, @PathVariable Long optionId,
                          @Valid @RequestBody ScoreRequest request) {
        boardService.setScores(auth.getName(), id, optionId, request);
    }

    @GetMapping("/{id}/comparison")
    public ComparisonResponse compare(Authentication auth, @PathVariable Long id) {
        return boardService.compare(auth.getName(), id);
    }
}
