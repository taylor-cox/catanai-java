import os
from typing import List, Tuple
import numpy as np
import torch as T
import torch.nn as nn
import torch.optim as optim
from torch.cuda.amp import autocast, GradScaler
from torch.distributions.categorical import Categorical
from torch.autograd import Variable
from torch.types import Number
from tqdm import tqdm
from ml.ppo_memory import PPOMemory
# from numba import njit, jit, prange


class FullyConnectedActorNetwork(nn.Module):
  def __init__(self, n_actions, input_dims: Tuple[int], alpha, chkpt_dir='tmp/ppo') -> None:
    super(FullyConnectedActorNetwork, self).__init__()
    self.chkpt_dir = chkpt_dir
    '''MODEL 1'''
    self.network_to_use = nn.Sequential(
      nn.Linear(*input_dims, 600),
      nn.ReLU(),
      nn.Linear(600, 600),
      nn.ReLU(),
      nn.Linear(600, 300),
      nn.ReLU(),
      nn.Linear(300, 256),
      nn.ReLU(),
      # nn.Softmax(dim=-1)
    ).to("cuda:0")
    '''MODEL 2'''
    # self.encoder = nn.TransformerEncoder(
    #     nn.TransformerEncoderLayer(*input_dims, 11), num_layers=6
    #   ).to("cuda:0")
    
    # self.decoder = nn.TransformerDecoder(
    #     nn.TransformerDecoderLayer(*input_dims, 11), num_layers=6
    #   ).to("cuda:0")
    
    # self.linear = nn.Linear(*input_dims, 11).to("cuda:0")
    # self.relu = nn.ReLU().to("cuda:0")

    self.policy_outputs = nn.ModuleList(
      [
        nn.Linear(256, 15),
        nn.Linear(256, 72),
        nn.Linear(256, 72),
        nn.Linear(256, 10),
        nn.Linear(256, 10),
        nn.Linear(256, 10),
        nn.Linear(256, 10),
        nn.Linear(256, 10),
        nn.Linear(256, 10),
        nn.Linear(256, 10),
        nn.Linear(256, 10),
      ]
    )

    self.optimizer = optim.SGD(self.parameters(), lr=alpha, momentum=0.9)
    self.scheduler = T.optim.lr_scheduler.CyclicLR(self.optimizer, base_lr=0.01, max_lr=0.1)
    self.device = T.device("cuda:0" if T.cuda.is_available() else 'cpu')
    self.to(self.device)

  # @autocast()
  def forward(self, state):
    # encoder_outputs = self.encoder(state)
    # decoder_outputs = self.decoder(encoder_outputs, state)
    # logits = self.linear(decoder_outputs)
    # logits = self.relu(logits) 
    logits = self.network_to_use(state)
    policy_outputs = [T.softmax(head(logits), dim=-1) for head in self.policy_outputs]
    policy_outputs = [Categorical(logits=head) for head in policy_outputs]
    # print(logits)
    return policy_outputs
  
  def save_checkpoint(self, player_id: str):
    checkpoint_file = os.path.join(self.chkpt_dir, f'actor_torch_ppo_{player_id}.pt')
    T.save(self.state_dict(), checkpoint_file)

  def load_checkpoint(self, player_id: str):
    try:
      checkpoint_file = os.path.join(self.chkpt_dir, f'actor_torch_ppo_{player_id}.pt')
      self.load_state_dict(T.load(checkpoint_file))
    except:
      print("No checkpoint found. Creating new model...")
  
class FullyConnectedCriticNetwork(nn.Module):
  def __init__(self, input_dims: Tuple[int], alpha, chkpt_dir='tmp/ppo'):
    super(FullyConnectedCriticNetwork, self).__init__()
    self.chkpt_dir = chkpt_dir
    '''MODEL 1'''
    self.network_to_use = nn.Sequential(
      nn.Linear(*input_dims, 600),
      nn.ReLU(),
      nn.Linear(600, 600),
      nn.ReLU(),
      nn.Linear(600, 300),
      nn.ReLU(),
      nn.Linear(300, 256),
      nn.ReLU(),
      nn.Linear(256, 1),
      nn.ReLU(),
      # nn.Softmax(dim=-1)
    ).to("cuda:0")

    '''MODEL 2'''
    # self.encoder = nn.TransformerEncoder(
    #     nn.TransformerEncoderLayer(*input_dims, 11), num_layers=6
    #   ).to("cuda:0")
    
    # self.decoder = nn.TransformerDecoder(
    #     nn.TransformerDecoderLayer(*input_dims, 11), num_layers=6
    #   ).to("cuda:0")
    
    # self.linear = nn.Linear(*input_dims, 11).to("cuda:0")
    # self.sigmoid = nn.Sigmoid().to("cuda:0")

    self.optimizer = optim.SGD(self.parameters(), lr=alpha, momentum=0.9)
    self.scheduler = T.optim.lr_scheduler.CyclicLR(self.optimizer, base_lr=0.01, max_lr=0.1)
    self.device = T.device("cuda:0" if T.cuda.is_available() else 'cpu')
    self.to(self.device)
  
  # @autocast()
  def forward(self, state):
    # encoder_outputs = self.encoder(state)
    # decoder_outputs = self.decoder(encoder_outputs, state)
    # logits = self.linear(decoder_outputs)
    logits = self.network_to_use(state)
    return logits
  
  def save_checkpoint(self, player_id: str):
    checkpoint_file = os.path.join(self.chkpt_dir, f'critic_torch_ppo_{player_id}.pt')
    T.save(self.state_dict(), checkpoint_file)

  def load_checkpoint(self, player_id: str):
    try:
      self.checkpoint_file = os.path.join(self.chkpt_dir, f'critic_torch_ppo_{player_id}.pt')
      self.load_state_dict(T.load(self.checkpoint_file))
    except:
      print("No checkpoint found (critic). Creating new model...")

class FullyConnectedAgent:
  def __init__(
      self, 
      n_actions: int,
      input_dims: Tuple[int] | None,
      gamma: float = 0.99,
      alpha: float = 0.0003,
      gae_lambda: float = 0.95,
      policy_clip: float = 0.2,
      batch_size: int = 64,
      N: int = 2048,
      n_epochs: int = 10,
    ) -> None:
    if input_dims == None:
      raise Exception("input_dims must be specified")

    self.n_actions = n_actions
    self.gamma: float = gamma
    self.policy_clip: float = policy_clip
    self.n_epochs: int = n_epochs
    self.gae_lambda: float = gae_lambda
    self.batch_size: int = batch_size

    self.actor: FullyConnectedActorNetwork = FullyConnectedActorNetwork(n_actions, input_dims, alpha)
    self.critic: FullyConnectedCriticNetwork = FullyConnectedCriticNetwork(input_dims, alpha)
    self.memory: PPOMemory = PPOMemory(batch_size)

  def remember(self, state, action, probs, vals, reward, done):
    self.memory.store_memory(state, action, probs, vals, reward, done)
  
  def save_models(self, player_id: str):
    self.actor.save_checkpoint(player_id)
    self.critic.save_checkpoint(player_id)
  
  def load_models(self, player_id: str):
    print("Loading models.......")
    self.actor.load_checkpoint(player_id)
    self.critic.load_checkpoint(player_id)
  
  def choose_action(self, observation) -> Tuple[T.Tensor, T.Tensor, T.Tensor]:
    state = T.tensor(np.array(observation), dtype=T.float).to(self.actor.device)

    dists: List[Categorical] = self.actor(state)
    value: T.Tensor = self.critic(state)
    action: T.Tensor = T.tensor([T.squeeze(dist.sample()).item() for dist in dists]).to(self.actor.device)
    # probs = T.stack(dist.log_prob(action) for dist in dists).to(self.actor.device)
    probs: T.Tensor = T.tensor([dist.log_prob(action[i]).item() for i, dist in enumerate(dists)]).to('cpu')
    
    return action, probs, value

  # def learnHelper(self, iterator):
  #   state_arr, action_arr, old_probs_arr, vals_arr, reward_arr, done_arr, batches = iterator[0]
  #   actor = iterator[1]
  #   critic = iterator[2]
  #   advantage = np.zeros(len(reward_arr), dtype=np.float32)
  #   values = vals_arr

  #   total_losses = []

  #   for t in range(len(reward_arr) - 1):
  #     discount = 1
  #     a_t = 0
  #     for k in range(t, len(reward_arr) - 1):
  #       a_t += discount * (reward_arr[k] + self.gamma * values[k+1] * (1-int(done_arr[k])) - values[k])
  #       discount *= self.gamma * self.gae_lambda
  #     advantage[t] = a_t
  #   # advantage = T.tensor(advantage, device=self.actor.device)

  #   for batch in batches:
  #     states = state_arr[batch]
  #     old_probs = old_probs_arr[batch]
  #     actions = action_arr[batch]

  #     dist = actor(states)
  #     critic_value = critic(states)

  #     critic_value = T.squeeze(critic_value)
  #     new_probs = []

  #     for i in range(self.n_actions):
  #       new_probs.append([])
  #       for j in range(self.n_actions):
  #         new_probs[i].append(dist[j].log_prob(actions[i][j])[0])
          
  #     new_probs = T.stack([T.stack(new_p) for new_p in new_probs]).to(self.actor.device)

  #     prob_ratio = new_probs.exp() / old_probs.exp()
  #     weighted_probs = advantage[batch] * prob_ratio
  #     weighted_clipped_probs = T.clamp(prob_ratio, 1-self.policy_clip, 1+self.policy_clip)*advantage[batch]
  #     actor_loss = -T.min(weighted_probs, weighted_clipped_probs).mean()

  #     returns = advantage[batch] + values[batch]

  #     critic_loss = (returns-critic_value)**2
  #     critic_loss = critic_loss.mean()
  #     total_loss = actor_loss + 0.5*critic_loss
  #     total_losses.append(total_loss)

  #   # Aquire semaphore
  #   # self.semaphore_actor_critic.acquire()
  #   for total_loss in total_losses:
  #     self.actor.optimizer.zero_grad()
  #     self.critic.optimizer.zero_grad()
  #     total_loss.backward()
  #     self.actor.optimizer.step()
  #     self.critic.optimizer.step()
  #   # self.semaphore_actor_critic.release()
  #   # Release semaphore
  
  def learn(self):
    # TODO: Parallelize testing, can remove.
    # toIterateOver = [(self.memory.generate_batches(), pickle.loads(pickle.dumps(self.actor)), pickle.loads(pickle.dumps(self.critic))) for _ in range(self.n_epochs)]
    # self.current_epoch_processing = 0
    # self.semaphore_actor_critic = Semaphore()

    # with ThreadPoolExecutor(max_workers=10) as executor:
    #   futures = [executor.submit(self.learnHelper, iterable) for iterable in toIterateOver]
    #   for future in futures:
    #     future.result()


    for _ in range(self.n_epochs):
      state_arr, action_arr, old_probs_arr, vals_arr, reward_arr, done_arr, batches = self.memory.generate_batches()
      advantage = np.zeros(len(reward_arr), dtype=np.float32)
      values = vals_arr

      # def calcAdvantage(t):
      #   a_t = 0
      #   discount = 1
      #   for k in range(t, len(reward_arr) - 1):
      #     a_t += discount * (reward_arr[k] + self.gamma * values[k+1] * (1-int(done_arr[k])) - values[k])
      #     discount *= self.gamma * self.gae_lambda
      #   return a_t

      # advantage = T.tensor(np.array(list(map(calcAdvantage, range(len(reward_arr) - 1))), dtype=np.float32), device=self.actor.device, dtype=T.float32)

      for t in range(len(reward_arr) - 1):
        discount = 1
        a_t = 0
        for k in range(t, len(reward_arr) - 1):
          a_t += discount * (reward_arr[k] + self.gamma * values[k+1] * (1-int(done_arr[k])) - values[k])
          discount *= self.gamma * self.gae_lambda
        advantage[t] = a_t
      advantage = T.tensor(advantage, device=self.actor.device)

      for i, batch in enumerate(batches):
        states = T.tensor(state_arr[batch], dtype=T.float32, device=self.actor.device)
        old_probs = T.tensor(old_probs_arr[batch], dtype=T.float32, device=self.actor.device)
        actions = T.tensor(action_arr[batch], dtype=T.float32, device=self.actor.device)
        newVal = T.tensor(values[batch], device=self.actor.device)

        dist = self.actor(states)
        critic_value = self.critic(states)

        critic_value = T.squeeze(critic_value)
        new_probs = []

        for i in range(self.batch_size):
          new_probs.append([])
          for j in range(self.n_actions):
            new_probs[i].append(dist[j].log_prob(actions[i][j])[0])
            
        new_probs = T.stack([T.stack(new_p) for new_p in new_probs]).to(self.actor.device)
        prob_ratio = new_probs.exp() / old_probs.exp()
        weighted_probs = T.matmul(advantage[batch], prob_ratio)
        weighted_clipped_probs = T.matmul(advantage[batch], T.clamp(prob_ratio, 1-self.policy_clip, 1+self.policy_clip))
        actor_loss = -T.min(weighted_probs, weighted_clipped_probs).mean()
        returns = advantage[batch] + newVal

        critic_loss = (returns-critic_value)**2
        critic_loss = critic_loss.mean()

        total_loss = actor_loss + 0.5*critic_loss
        self.actor.optimizer.zero_grad(set_to_none=True)
        self.critic.optimizer.zero_grad(set_to_none=True)
        total_loss.backward()
        self.actor.optimizer.step()
        self.critic.optimizer.step()
        self.actor.scheduler.step()
        self.critic.scheduler.step()
    
    self.memory.clear_memory()