import gym
import numpy as np
from ppo import Agent
from dao.dao import GameStateDAO
from websockets.game_websockets import GameWebSocketHandler
import json

def run_training():
  env = gym.make("CartPole-v1")
  N = 20
  batch_size = 5
  n_epochs = 4
  alpha = 0.0003
  agent = Agent(
    n_actions = env.action_space.n,  # type: ignore
    batch_size=batch_size,
    alpha=alpha,
    n_epochs=n_epochs,
    input_dims=env.observation_space.shape
  )

  print(env.action_space.n) # type: ignore

  n_games = 300

  figure_file = 'plots/cartpole.png'
  best_score = env.reward_range[0]
  score_history = []

  learn_iters = 0
  avg_score = 0
  n_steps = 0

  for i in range(n_games):
    observation, _ = env.reset()
    done = False
    score = 0
    print_once = True
    while not done:
      action, prob, val = agent.choose_action(observation)
      observation_, reward, done, terminated,  info = env.step(action)
      if print_once:
        print_once = False
        print(f'Observation: {observation}')
        print(f'Action: {action}\nProb: {prob}\nVal: {val}')
        print(f'Observation_: {observation_}\nReward: {reward}\nDone: {done}\nTerminated: {terminated}\nInfo: {info}')
      n_steps += 1
      score += reward
      if n_steps % N == 0:
        agent.learn()
        learn_iters += 1
      observation = observation_
    score_history.append(score)
    avg_score = np.mean(score_history[-100:])

    if avg_score > best_score:
      best_score = avg_score
      agent.save_models()

    print(f'--------------------------\nEpisode: {i}\nScore: {score}\nAverage Score: {avg_score}\nTime steps: {n_steps}\nLearning steps: {learn_iters}')

if __name__ == '__main__':
  # gameStateDao = GameStateDAO()
  # with GameWebSocketHandler('ws://192.168.1.108:8080/game') as websocketHandler:
  #   gamestateString = websocketHandler.newGame()
  #   gameStateDao.addGamestate(gamestateString, 0)
  #   allPlayersMoves = [
  #     {'1': [ [2, 0],  [1, 0],  [2, 2],  [1, 4]]},
  #     {'2': [ [2, 7],  [1, 10], [2, 10], [1, 17]]},
  #     {'3': [ [2, 16], [1, 23], [2, 20], [1, 32]]},
  #     {'4': [ [2, 33], [1, 49], [2, 37], [1, 53]]},
  #   ]

  #   for player in allPlayersMoves:
  #     for id in player:
  #       for move in player[id]:
  #         output = websocketHandler.addMove(playerID=id, action=move)
  #         if json.loads(output)['success'] != True:
  #           print(f'Error: {output}')
    
  #   for _ in range(16):
  #     gamestateString = websocketHandler.makeMove()
  #     gameStateDao.addGamestate(gamestateString, 0)
  pass

  # TODO
  # 1. Create 4 actors for the game.
  # 2. Create new game.
  # 3. Ask actor for move, add move to corresponding player.
  # 4. Execute move (makeMove).
  #  