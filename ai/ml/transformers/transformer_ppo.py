import os
from typing import List, Tuple
import numpy as np
import torch as T
import torch.nn as nn
import torch.optim as optim
from torch.distributions.categorical import Categorical
from torch.types import Number
from tqdm import tqdm
from ml.ppo_memory import PPOMemory
from ml.transformers.positional_encoder import PositionalEncoding
# from numba import njit


# TODO: Work in progress

# class FullyConnectedActorNetwork(nn.Module):
#   def __init__(
#       self,
#       n_actions,
#       input_dims: Tuple[int],
#       alpha,
#       chkpt_dir='tmp/ppo',
#       dim_model=20,
#       num_tokens=16,
#       num_heads=16,
#       num_encoder_layers=6,
#       num_decoder_layers=6,
#       dropout=0.1,
#     ) -> None:
#     super(FullyConnectedActorNetwork, self).__init__()

#     self.checkpoint_file = os.path.join(chkpt_dir, 'actor_transformer_ppo')
#     # LAYERS
#     self.positional_encoder = PositionalEncoding(
#         dim_model=dim_model, dropout_p=dropout, max_len=5000
#     )
#     self.embedding = nn.Embedding(num_tokens, dim_model)
#     self.transformer = nn.Transformer(
#         d_model=dim_model,
#         nhead=num_heads,
#         num_encoder_layers=num_encoder_layers,
#         num_decoder_layers=num_decoder_layers,
#         dropout=dropout,
#     )

#     self.policy_outputs = nn.ModuleList(
#       [
#         nn.Linear(512, 15),
#         nn.Linear(512, 72),
#         nn.Linear(512, 72),
#         nn.Linear(512, 10),
#         nn.Linear(512, 10),
#         nn.Linear(512, 10),
#         nn.Linear(512, 10),
#         nn.Linear(512, 10),
#         nn.Linear(512, 10),
#         nn.Linear(512, 10),
#         nn.Linear(512, 10),
#       ]
#     )

#     self.optimizer = optim.SGD(self.parameters(), lr=alpha)
#     self.device = T.device("cuda:0" if T.cuda.is_available() else 'cpu')
#     self.to(self.device)

#   def forward(self, state):
#     # encoder_outputs = self.encoder(state)
#     # decoder_outputs = self.decoder(encoder_outputs, state)
#     # logits = self.linear(decoder_outputs)
#     # logits = self.relu(logits) 
#     logits = self.network_to_use(state)
#     policy_outputs = [T.softmax(head(logits), dim=-1) for head in self.policy_outputs]
#     policy_outputs = [Categorical(logits=head) for head in policy_outputs]
#     # print(logits)
#     return policy_outputs
  
#   def save_checkpoint(self):
#     T.save(self.state_dict(), self.checkpoint_file)

#   def load_checkpoint(self):
#     try:
#       self.load_state_dict(T.load(self.checkpoint_file))
#     except:
#       print("No checkpoint found. Creating new model...")
  
# class FullyConnectedCriticNetwork(nn.Module):
#   def __init__(self, input_dims: Tuple[int], alpha, chkpt_dir='tmp/ppo'):
#     super(FullyConnectedCriticNetwork, self).__init__()
#     self.checkpoint_file = os.path.join(chkpt_dir, 'actor_torch_ppo')
#     '''MODEL 1'''
#     self.network_to_use = nn.Sequential(
#       nn.Linear(*input_dims, 512),
#       nn.ReLU(),
#       nn.Linear(512, 256),
#       nn.ReLU(),
#       nn.Linear(256, 128),
#       nn.ReLU(),
#       nn.Linear(128, 64),
#       nn.ReLU(),
#       nn.Linear(64, 32),
#       nn.ReLU(),
#       nn.Linear(32, 1),
#       # nn.Softmax(dim=-1)
#     ).to("cuda:0")

#     '''MODEL 2'''
#     # self.encoder = nn.TransformerEncoder(
#     #     nn.TransformerEncoderLayer(*input_dims, 11), num_layers=6
#     #   ).to("cuda:0")
    
#     # self.decoder = nn.TransformerDecoder(
#     #     nn.TransformerDecoderLayer(*input_dims, 11), num_layers=6
#     #   ).to("cuda:0")
    
#     # self.linear = nn.Linear(*input_dims, 11).to("cuda:0")
#     # self.sigmoid = nn.Sigmoid().to("cuda:0")

#     self.optimizer = optim.Adam(self.parameters(), lr=alpha)
#     self.device = T.device("cuda:0" if T.cuda.is_available() else 'cpu')
#     self.to(self.device)
  
#   def forward(self, state):
#     # encoder_outputs = self.encoder(state)
#     # decoder_outputs = self.decoder(encoder_outputs, state)
#     # logits = self.linear(decoder_outputs)
#     logits = self.network_to_use(state)
#     return logits
  
#   def save_checkpoint(self):
#     T.save(self.state_dict(), self.checkpoint_file)

#   def load_checkpoint(self):
#     try:
#       self.load_state_dict(T.load(self.checkpoint_file))
#     except:
#       print("No checkpoint found (critic). Creating new model...")

# class FullyConnectedAgent:
#   def __init__(
#       self, 
#       n_actions: int,
#       input_dims: Tuple[int] | None,
#       gamma: float = 0.99,
#       alpha: float = 0.0003,
#       gae_lambda: float = 0.95,
#       policy_clip: float = 0.2,
#       batch_size: int = 64,
#       N: int = 2048,
#       n_epochs: int = 10,
#     ) -> None:
#     if input_dims == None:
#       raise Exception("input_dims must be specified")

#     self.gamma: float = gamma
#     self.policy_clip: float = policy_clip
#     self.n_epochs: int = n_epochs
#     self.gae_lambda: float = gae_lambda

#     self.actor: FullyConnectedActorNetwork = FullyConnectedActorNetwork(n_actions, input_dims, alpha)
#     self.critic: FullyConnectedCriticNetwork = FullyConnectedCriticNetwork(input_dims, alpha)
#     self.memory: PPOMemory = PPOMemory(batch_size)

#   def remember(self, state, action, probs, vals, reward, done):
#     self.memory.store_memory(state, action, probs, vals, reward, done)
  
#   def save_models(self):
#     print('Saving models......')
#     self.actor.save_checkpoint()
#     self.critic.save_checkpoint()
  
#   def load_models(self):
#     print("Loading models.......")
#     self.actor.load_checkpoint()
#     self.critic.load_checkpoint()
  
#   # def choose_action(self, observation) -> Tuple[T.Tensor, T.Tensor, T.Tensor]:
#   def choose_action(self, observation) -> Tuple[T.Tensor, T.Tensor, T.Tensor]:
#     state = T.tensor([observation], dtype=T.float).to(self.actor.device)

#     dists: List[Categorical] = self.actor(state)
#     value: T.Tensor = self.critic(state)
#     action: T.Tensor = T.tensor([T.squeeze(dist.sample()).item() for dist in dists]).to(self.actor.device)
#     # probs = T.stack(dist.log_prob(action) for dist in dists).to(self.actor.device)
#     probs: T.Tensor = T.tensor([dist.log_prob(action[i]).item() for i, dist in enumerate(dists)]).to('cpu')
    
#     return action, probs, value
  
#   def learn(self):
#     for _ in tqdm(range(self.n_epochs), leave=False):
#       state_arr, action_arr, old_probs_arr, vals_arr, reward_arr, done_arr, batches = self.memory.generate_batches()
#       values = vals_arr
#       advantage = np.zeros(len(reward_arr), dtype=np.float32)

#       for t in range(len(reward_arr) - 1):
#         discount = 1
#         a_t = 0
#         for k in range(t, len(reward_arr) - 1):
#           a_t += discount * (reward_arr[k] + self.gamma * values[k+1] * (1-int(done_arr[k])) - values[k])
#           discount *= self.gamma * self.gae_lambda
#         advantage[t] = a_t
#       advantage = T.tensor(advantage).to(self.actor.device)

#       # print(advantage)

#       values = T.tensor(values).to(self.actor.device)
#       for batch in batches:
#         states = T.tensor(state_arr[batch], dtype=T.float).to(self.actor.device)
#         old_probs = T.tensor(old_probs_arr[batch]).to(self.actor.device)
#         actions = T.tensor(action_arr[batch]).to(self.actor.device)
#         # print("======================================")
#         # print(states)
#         # print(old_probs)
#         # print(actions)
#         # print("======================================")
#         # input("States, old_probs, actions")

#         dist = self.actor(states)
#         critic_value = self.critic(states)
#         # print("======================================")
#         # print(dist)
#         # print(critic_value)
#         # print("======================================")
#         # input("dist, critic_value")

#         critic_value = T.squeeze(critic_value)
#         new_probs = []

#         for i in range(11):
#           new_probs.append([])
#           for j in range(11):
#             new_probs[i].append(dist[j].log_prob(actions[i][j])[0])
            
#         new_probs = T.stack([T.stack(new_p) for new_p in new_probs]).to(self.actor.device)

#         # print("======================================")
#         # print(new_probs)
#         # print(old_probs)
#         # print("======================================")
#         # input("new_probs, old_probs")
#         prob_ratio = new_probs.exp() / old_probs.exp()
#         # print("======================================")
#         # print(prob_ratio)
#         # print(advantage[batch])
#         # print(batch)
#         # print(advantage)
#         # print("======================================")
#         # input()
#         weighted_probs = advantage[batch] * prob_ratio
#         weighted_clipped_probs = T.clamp(prob_ratio, 1-self.policy_clip, 1+self.policy_clip)*advantage[batch]
#         actor_loss = -T.min(weighted_probs, weighted_clipped_probs).mean()

#         returns = advantage[batch] + values[batch]

#         critic_loss = (returns-critic_value)**2
#         critic_loss = critic_loss.mean()

#         total_loss = actor_loss + 0.5*critic_loss
#         self.actor.optimizer.zero_grad()
#         self.critic.optimizer.zero_grad()
#         total_loss.backward()
#         self.actor.optimizer.step()
#         self.critic.optimizer.step()
    
#     self.memory.clear_memory()