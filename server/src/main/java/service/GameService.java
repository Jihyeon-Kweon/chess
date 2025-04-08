package service;

import chess.ChessGame;
import chess.ChessMove;
import chess.InvalidMoveException;
import dataaccess.AuthDAO;
import dataaccess.GameDAO;
import dataaccess.DataAccessException;
import model.AuthData;
import model.GameData;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class GameService {
    private final GameDAO gameDAO;
    private final AuthDAO authDAO;

    public GameService(GameDAO gameDAO, AuthDAO authDAO) {
        this.gameDAO = gameDAO;
        this.authDAO = authDAO;
    }

    public List<GameData> listGames(String authToken) throws DataAccessException {
        AuthData auth = authDAO.getAuth(authToken);
        if (auth == null) {
            throw new DataAccessException("Error: unauthorized");
        }

        List<GameData> games = gameDAO.listGames();
        System.out.println("Retrieved games: " + games);

        if (games == null) {
            return new ArrayList<>(); // null 방지
        }
        return games;
    }


    public GameData createGame(String authToken, String gameName) throws DataAccessException {
        AuthData auth = authDAO.getAuth(authToken);
        if (auth == null) {
            throw new DataAccessException("Error: unauthorized");
        }
        if (gameName == null || gameName.isEmpty()) {
            throw new DataAccessException("Error: bad request");
        }

        // ✅ 새 ChessGame 객체 생성
        ChessGame newGameState = new ChessGame();

        GameData game = new GameData(0, null, null, gameName, newGameState);
        int gameID = gameDAO.createGame(game);

        // ✅ 저장된 game 반환
        GameData savedGame = new GameData(gameID, null, null, gameName, newGameState);
        return savedGame;
    }


    public void joinGame(String authToken, int gameID, String playerColor) throws DataAccessException {
        AuthData auth = authDAO.getAuth(authToken);
        if (auth == null) {
            throw new DataAccessException("Error: unauthorized");
        }

        GameData game = gameDAO.getGame(gameID);
        if (game == null) {
            throw new DataAccessException("Error: bad request");
        }

        if (!"WHITE".equalsIgnoreCase(playerColor) && !"BLACK".equalsIgnoreCase(playerColor)) {
            throw new DataAccessException("Error: bad request");
        }


        // **WHITE 자리 체크**
        if ("WHITE".equalsIgnoreCase(playerColor)) {
            if (game.whiteUsername() != null) {
                throw new DataAccessException("Error: already taken");
            }
            game = new GameData(game.gameID(), auth.username(), game.blackUsername(), game.gameName(), game.game());
        }

        // **BLACK 자리 체크**
        else if ("BLACK".equalsIgnoreCase(playerColor)) {
            if (game.blackUsername() != null) {
                throw new DataAccessException("Error: already taken");
            }
            game = new GameData(game.gameID(), game.whiteUsername(), auth.username(), game.gameName(), game.game());
        }

        gameDAO.updateGame(game);
    }

    public GameData observeGame(String authToken, int gameID) throws DataAccessException {
        AuthData auth = authDAO.getAuth(authToken);
        if (auth == null) {
            throw new DataAccessException("Error: unauthorized");
        }

        GameData game = gameDAO.getGame(gameID);
        if (game == null) {
            throw new DataAccessException("Error: bad request");
        }

        return game;
    }

    public ChessGame getGame(int gameID, String authToken) throws DataAccessException {
        AuthData auth = authDAO.getAuth(authToken);
        if (auth == null) throw new DataAccessException("Error: unauthorized");

        GameData game = gameDAO.getGame(gameID);
        if (game == null) throw new DataAccessException("Error: bad request");

        return game.game();
    }

    public ChessGame makeMove(int gameID, String authToken, ChessMove move)
            throws DataAccessException, InvalidMoveException {

        // 1. 인증 토큰 검증
        AuthData auth = authDAO.getAuth(authToken);
        if (auth == null) {
            throw new DataAccessException("Error: unauthorized");
        }

        // 2. 게임 데이터 불러오기
        GameData gameData = gameDAO.getGame(gameID);
        if (gameData == null) {
            throw new DataAccessException("Error: bad request");
        }

        ChessGame game = gameData.game();
        if (game == null) {
            throw new DataAccessException("Error: no game state");
        }

        // 3. 게임 종료 여부 확인
        if (game.isGameOver()) {
            throw new DataAccessException("Error: game is already over");
        }

        // 4. 유저가 플레이어인지 확인
        String username = auth.username();
        ChessGame.TeamColor playerColor;
        if (username.equals(gameData.whiteUsername())) {
            playerColor = ChessGame.TeamColor.WHITE;
        } else if (username.equals(gameData.blackUsername())) {
            playerColor = ChessGame.TeamColor.BLACK;
        } else {
            throw new DataAccessException("Error: only players can move");
        }

        // 5. 턴이 맞는지 확인
        if (game.getTeamTurn() != playerColor) {
            throw new DataAccessException("Error: not your turn");
        }

        // 6. 유효한 이동인지 확인
        var validMoves = game.validMoves(move.getStartPosition());
        if (validMoves == null || !validMoves.contains(move)) {
            throw new InvalidMoveException("Error: invalid move");
        }

        // 7. 이동 수행 (예외 발생 가능)
        game.makeMove(move);

        // 8. 게임 데이터 업데이트
        gameDAO.updateGame(new GameData(
                gameData.gameID(),
                gameData.whiteUsername(),
                gameData.blackUsername(),
                gameData.gameName(),
                game
        ));

        return game;
    }



    public void leaveGame(int gameID, String authToken) throws DataAccessException {
        AuthData auth = authDAO.getAuth(authToken);
        if (auth == null) throw new DataAccessException("Error: unauthorized");

        GameData game = gameDAO.getGame(gameID);
        if (game == null) throw new DataAccessException("Error: bad request");

        String username = auth.username();
        GameData updated = game;

        if (username.equals(game.whiteUsername())) {
            updated = new GameData(game.gameID(), null, game.blackUsername(), game.gameName(), game.game());
        } else if (username.equals(game.blackUsername())) {
            updated = new GameData(game.gameID(), game.whiteUsername(), null, game.gameName(), game.game());
        }

        gameDAO.updateGame(updated);
    }




}