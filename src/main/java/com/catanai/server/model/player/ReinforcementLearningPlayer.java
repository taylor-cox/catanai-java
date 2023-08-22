package com.catanai.server.model.player;

import com.catanai.server.model.gamestate.GameState;

import ai.djl.Model;
import ai.djl.modality.rl.agent.QAgent;
import ai.djl.nn.Activation;
import ai.djl.nn.Blocks;
import ai.djl.nn.SequentialBlock;
import ai.djl.nn.core.Linear;
import ai.djl.training.DefaultTrainingConfig;
import ai.djl.training.Trainer;
import ai.djl.training.loss.Loss;

/**
 * Reinforcement Learning Player for Catan.
 */
public final class ReinforcementLearningPlayer extends Player {

  public ReinforcementLearningPlayer(PlayerId id) {
    super(id);
  }

  private void buildModel() {
    long inputSize = 286;
    long outputSize = 11;

    // Build model block.
    SequentialBlock seqBlock = new SequentialBlock();
    seqBlock.add(Blocks.batchFlattenBlock(inputSize));
    seqBlock.add(Linear.builder().setUnits(350).build());
    seqBlock.add(Activation::relu);
    seqBlock.add(Linear.builder().setUnits(175).build());
    seqBlock.add(Activation::relu);
    seqBlock.add(Linear.builder().setUnits(90).build());
    seqBlock.add(Activation::relu);
    seqBlock.add(Linear.builder().setUnits(30).build());
    seqBlock.add(Activation::relu);
    seqBlock.add(Linear.builder().setUnits(outputSize).build());

    // Create model.
    Model model = Model.newInstance("tabular");
    model.setBlock(seqBlock);

    // Create training config.
    DefaultTrainingConfig config = new DefaultTrainingConfig(Loss.softmaxCrossEntropyLoss());
    Trainer trainer = model.newTrainer(config);
    QAgent qagent = new QAgent(trainer, );
    qagent.chooseAction(null, hasFinishedTurn);
  }

  @Override
  public int[] play(GameState gameState) {
    // TODO Auto-generated method stub
    // throw new UnsupportedOperationException("Unimplemented method 'play'");
  }
}
