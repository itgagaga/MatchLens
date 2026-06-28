package com.zzx.matchlens.controller;

import com.zzx.matchlens.entity.Team;
import com.zzx.matchlens.mapper.TeamMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/teams")
public class TeamController {

    private final TeamMapper teamMapper;

    public TeamController(TeamMapper teamMapper) {
        this.teamMapper = teamMapper;
    }

    @GetMapping
    public List<Team> getAllTeams() {
        return teamMapper.selectList(null);
    }
}
