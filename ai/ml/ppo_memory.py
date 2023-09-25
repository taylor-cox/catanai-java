import numpy as np
from typing import List, Tuple
import torch as T

class PPOMemory:
  def __init__(self, batch_size: int) -> None:
    # self.states: List[T.Tensor] = []
    # self.probs: List[T.Tensor] = []
    # self.vals: List[T.Tensor] = []
    # self.actions: List[T.Tensor] = []
    # self.rewards: List[T.Tensor] = []
    # self.dones: List[T.Tensor] = []
    
    self.states: np.ndarray | None = None
    self.probs: np.ndarray | None = None
    self.vals: np.ndarray | None = None
    self.actions: np.ndarray | None = None
    self.rewards: np.ndarray | None = None
    self.dones: np.ndarray | None = None
  
    self.batch_size = batch_size
  
  def generate_batches(
      self
    ) -> Tuple[
      np.ndarray,
      np.ndarray,
      np.ndarray,
      np.ndarray,
      np.ndarray,
      np.ndarray,
      np.ndarray
    ]:
    n_states = len(self.states) # type: ignore
    batch_start = np.arange(0, n_states, self.batch_size)
    indices = np.arange(n_states, dtype=np.int64)
    np.random.shuffle(indices)
    batches = np.array([indices[i:i+self.batch_size] for i in batch_start])

    # return T.stack(self.states), \
    #   T.stack(self.actions), \
    #   T.stack(self.probs), \
    #   T.stack(self.vals), \
    #   T.stack(self.rewards), \
    #   T.stack(self.dones), \
    #   T.tensor(batches).to("cuda:0")
    return self.states, \
      self.actions, \
      self.probs, \
      self.vals, \
      self.rewards, \
      self.dones, \
      batches.astype(int) # type: ignore
  
  def store_memory(
      self,
      state: np.ndarray,
      action: List[int],
      probs: T.Tensor,
      val: T.Tensor,
      reward: float,
      done: bool
    ) -> None:
    # stateSave: T.Tensor = T.tensor(state, dtype=T.float).to('cuda:0')
    # actionSave: T.Tensor = T.tensor(action, dtype=T.float).to('cuda:0')
    # probsSave: T.Tensor = probs.to('cuda:0')
    # valSave: T.Tensor = val[0].to('cuda:0')
    # rewardSave: T.Tensor = T.tensor(reward, dtype=T.float).to('cuda:0')
    # self.states.append(stateSave)
    # self.actions.append(actionSave)
    # self.probs.append(probsSave)
    # self.vals.append(valSave)
    # self.rewards.append(rewardSave)
    # self.dones.append(T.tensor(done))
    stateSave = state
    actionSave = np.array(action)
    probsSave = probs.to('cpu').numpy()
    valSave = val[0].to('cpu').detach().numpy()
    rewardSave = np.array(reward, ndmin=1)
    doneSave = np.array(done, ndmin=1)
    if self.states is None or self.actions is None or self.probs is None or self.vals is None or self.rewards is None or self.dones is None:
      self.states = stateSave
      self.actions = actionSave
      self.probs = probsSave
      self.vals = valSave
      self.rewards = np.array(rewardSave, ndmin=1)
      self.dones = np.array(doneSave, ndmin=1)
    else:
      self.states = np.vstack([self.states, stateSave])
      self.actions = np.vstack([self.actions, actionSave])
      self.probs = np.vstack([self.probs, probsSave])
      self.vals = np.vstack([self.vals, valSave])
      self.rewards = np.append(self.rewards, rewardSave)
      self.dones = np.append(self.dones, doneSave)
  
  def clear_memory(self):
    self.states = None
    self.actions = None
    self.probs = None
    self.vals = None
    self.rewards = None
    self.dones = None