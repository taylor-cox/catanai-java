package com.catanai.server.model.board.graph;

import com.catanai.server.model.board.tile.Port;
import com.catanai.server.model.board.tile.Tile;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

/**
* Class for handling linking of nodes to other objects.
*/
public final class NodeMapper {
  /**
  * Maps node and edges to each other.
  *
  * @param nodes nodes to map to edges
  * @param edges edges to map to nodes
  * @return boolean if the operation was successful.
  * @throws ArrayIndexOutOfBoundsException if edges or nodes is incorrect length 
  *     (72, 54 respectively)
  */
  public boolean mapNodesToEdges(List<Node> nodes, List<Edge> edges) 
      throws ArrayIndexOutOfBoundsException {
    try {
      File nodeEdgeMappingFile = new File("node_edge_mapping.txt");
      Scanner mappingScanner = new Scanner(nodeEdgeMappingFile);
      
      while (mappingScanner.hasNextLine()) {
        String mapping = mappingScanner.nextLine();
        String[] splitLine = mapping.split(":");
        // Get the node ID.
        int nodeId = Integer.parseInt(splitLine[0]) - 1;
        
        String[] edgeIdStrings = splitLine[1].split(",");
        ArrayList<Integer> edgeIds = new ArrayList<Integer>();
        for (String edgeId : edgeIdStrings) {
          edgeIds.add(Integer.parseInt(edgeId.strip()) - 1);
        }
        
        for (Integer edgeId : edgeIds) {
          nodes.get(nodeId).addConnectedEdge(edges.get(edgeId));
          edges.get(edgeId).addNode(nodes.get(nodeId));
        }
      }
      
      mappingScanner.close();
      
    } catch (FileNotFoundException e) {
      System.out.println("Could not find node edge mapping file.");
      e.printStackTrace();
      return false;
    }
    
    
    return true;
  }
  
  /**
  * Represents 2 nodes attached to a given port.
  */
  private static class PortNodes {
    public PortNodes(int n1, int n2) {
      this.n1 = n1;
      this.n2 = n2;
    }
    
    public int n1;
    public int n2;
  }
  
  /**
  * Maps nodes to random ports.
  *
  * @param nodes nodes to map to random ports
  * @return whether mapping was successful.
  */
  public boolean mapNodesToPorts(List<Node> nodes) {
    ArrayList<Port> ports = new ArrayList<Port>();
    // Add all possible ports to array.
    for (int i = 0; i < 4; i++) {
      ports.add(Port.THREE_TO_ONE);
    }
    ports.add(Port.BRICK_TWO_TO_ONE);
    ports.add(Port.GRAIN_TWO_TO_ONE);
    ports.add(Port.LUMBER_TWO_TO_ONE);
    ports.add(Port.ORE_TWO_TO_ONE);
    ports.add(Port.WOOL_TWO_TO_ONE);
    Collections.shuffle(ports);
    
    // Add all possible node indexes where ports can be present to array.
    ArrayList<PortNodes> portIndexes = new ArrayList<PortNodes>();
    portIndexes.add(new PortNodes(0, 3));
    portIndexes.add(new PortNodes(1, 5));
    portIndexes.add(new PortNodes(11, 16));
    portIndexes.add(new PortNodes(10, 15));
    portIndexes.add(new PortNodes(33, 38));
    portIndexes.add(new PortNodes(26, 32));
    portIndexes.add(new PortNodes(47, 51));
    portIndexes.add(new PortNodes(42, 46));
    portIndexes.add(new PortNodes(49, 52));
    
    for (int i = 0; i < ports.size(); i++) {
      PortNodes curNodes = portIndexes.get(i);
      Port curPort = ports.get(i);
      nodes.get(curNodes.n1).setPort(curPort);
      nodes.get(curNodes.n2).setPort(curPort);
    }
    
    return true;
  }
  
  /**
  * Maps nodes and tiles together.
  *
  * @param nodes nodes to map to tiles
  * @param tiles tiles to map to nodes
  * @return if mapping was sucessful
  */
  public boolean mapNodesToTiles(List<Node> nodes, List<Tile> tiles) {
    try {
      File nodeTileMappingFile = new File("node_tile_mapping.txt");
      Scanner mappingScanner = new Scanner(nodeTileMappingFile);
      
      while (mappingScanner.hasNextLine()) {
        String mapping = mappingScanner.nextLine();
        String[] splitLine = mapping.split(":");
        // Get the tile ID.
        int tileId = Integer.parseInt(splitLine[0]) - 1;
        
        // Get node IDs
        String[] nodeIdStrings = splitLine[1].split(",");
        ArrayList<Integer> nodeIds = new ArrayList<Integer>();
        for (String nodeId : nodeIdStrings) {
          nodeIds.add(Integer.parseInt(nodeId.strip()) - 1);
        }
        
        // Map node and tile IDs together.
        for (Integer nodeId : nodeIds) {
          tiles.get(tileId).addNode(nodes.get(nodeId));
          nodes.get(nodeId).addProduces(tiles.get(tileId).getTerrain());
        }
      }
      
      mappingScanner.close();
      
    } catch (FileNotFoundException e) {
      System.out.println("Could not find node tile mapping file.");
      e.printStackTrace();
      return false;
    }
    
    
    return true;
  }
}
