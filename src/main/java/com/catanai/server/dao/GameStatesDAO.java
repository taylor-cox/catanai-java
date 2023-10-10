package com.catanai.server.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import org.postgresql.jdbc.PgArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * Game states DAO implementation.
 */
@Repository
public class GameStatesDAO {
  @Autowired
  JdbcTemplate jdbcTemplate;

  class ColumnRawMapper extends ColumnMapRowMapper {
    @Override
    protected Object getColumnValue(ResultSet rs, int index) throws SQLException {
      Object obj = rs.getObject(index);
      if (obj instanceof PgArray) {
        return ((PgArray) obj).getArray();
      }
      return super.getColumnValue(rs, index);
    }
  }

  /**
   * Get all gamestates of the given gameId from the database.
   *
   * @param gameId id of the game
   * @return list of gamestates
   */
  public List<Map<String, Object>> getGameStates(int gameId) {
    String sql = "SELECT * FROM games.gamestates WHERE game_id = " + gameId + " ORDER BY id ASC;";
    return jdbcTemplate.query(
        sql,
        new ColumnRawMapper()
    );
  }

  /**
   * Get all starting turns row IDs of all games from the database.
   *
   * @return list of row IDs
   */
  public List<Map<String, Object>> getRowIDsOfFirstTurnInAllGames() {
    String sql = "SELECT MIN(id) AS id FROM games.gamestates WHERE game_id != 0 GROUP BY game_id ORDER BY id;";
    return jdbcTemplate.query(
        sql,
        new ColumnRawMapper()
    );
  }
}
