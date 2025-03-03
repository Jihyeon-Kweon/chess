package model;

import java.util.List;

public class GameList {
    private List<GameData> games;

    public GameList(List<GameData> games){
        this.games = games;
    }

    public List<GameData> getGames(){
        return games;
    }
}
