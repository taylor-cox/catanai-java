package com.catanai.server.model.action;

import java.util.Arrays;
import lombok.Getter;


/**
* Class representing other information about the current action.
* For instance, if Action == PLAY_ROAD, contains information about where to place
* said road.
*/
@Getter
public final class ActionMetadata {
  Action action;
  int[] metadata;
  int[] relevantMetadata;
  
  /**
   * Creates a new action metadata with @param metadata.
   *
   * @param metadata metadata pertaining to action.
   */
  public ActionMetadata(int[] metadata) {
    this.action = Action.valueOf(metadata[0]);
    this.metadata = metadata;
    this.populateRelevantMetadata();
  }
  
  /**
  * Populates the relevant metadata variable with all data pertaining to the
  * current action. For instance, if the action is PLAY_ROAD, then the relevant
  * metadata contains one value, [edge_placement].
  */
  private void populateRelevantMetadata() {
    switch (this.action) {
      case PLAY_SETTLEMENT:
      case PLAY_CITY:
        // Contains information about the node placement of the building.
        this.relevantMetadata = new int[] {metadata[1]};
        break;
      case PLAY_ROAD:
        // Contains information about the edge placement of the building.
        this.relevantMetadata = new int[] {metadata[1]};
        break;
      case PLAY_KNIGHT:
      case MOVE_ROBBER:
        // Contains information about the tile placement of the robber, and
        // the player to rob from.
        this.relevantMetadata = new int[] {metadata[1], metadata[2]};
        break;
      case PLAY_ROAD_BUILDING:
        // Contains information about the edge placements of the roads.
        this.relevantMetadata = new int[] {metadata[1], metadata[2]};
        break;
      case PLAY_YEAR_OF_PLENTY:
        // Contains information about the 2 cards to draw.
        this.relevantMetadata = new int[] {metadata[1], metadata[2]};
        break;
      case PLAY_MONOPOLY:
        // Contains information about the card to monopolize.
        this.relevantMetadata = new int[] {metadata[1]};
        break;
      case OFFER_TRADE:
        // Contains information about the amount of resource cards in trade.
        this.relevantMetadata = Arrays.copyOfRange(metadata, 1, 11);
        break;
      case DECLINE_TRADE:
      case ACCEPT_TRADE:
        // Contains information about the player to accept the trade from.
        this.relevantMetadata = new int[] {metadata[1]};
        break;
      case DRAW_DEVELOPMENT_CARD:
      case END_TURN:
        break;
      case DISCARD:
        // Contains information about the amount of types of resource cards to discard.
        this.relevantMetadata = Arrays.copyOfRange(metadata, 1, 6);
        break;
      default:
        break;
    }
  }

}
