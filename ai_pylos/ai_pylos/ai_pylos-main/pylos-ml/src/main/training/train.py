from matplotlib import pyplot as plt
import tensorflow as tf
from tensorflow.keras import layers, models
import json
import numpy as np
import datetime
import os

 #DATASET_PATH = "resources/games/0.json"
#DATASET_PATH = "pylos-ml/src/main/training/resources/games/0.json"
#MODEL_EXPORT_PATH = "resources/models/"
DATASET_PATH = "pylos-ml/src/main/training/resources/games/1731625595073.json"
MODEL_EXPORT_PATH = "resources/models/"
SELECTED_PLAYERS = []
DISCOUNT_FACTOR = 0.98
EPOCHS = 5 #100
BATCH_SIZE = 1024
N_CORES = 8

os.environ["OMP_NUM_THREADS"] = str(N_CORES)
os.environ["TF_NUM_INTRAOP_THREADS"] = str(N_CORES)
os.environ["TF_NUM_INTEROP_THREADS"] = str(N_CORES)

def main():
    print("TensorFlow version:", tf.__version__)

    model = build_model()

    model.compile(optimizer='adam', loss='mean_squared_error')

    boards, scores = build_dataset(DATASET_PATH)

    print("# datapoints:", len(boards))

    #print the dataset
    print("boards:", boards)
    print("scores:", scores)

    history = model.fit(boards, scores, epochs=EPOCHS, batch_size=BATCH_SIZE)

    # Save the model as SavedModel, with date and time as the name
    model.export(MODEL_EXPORT_PATH + datetime.datetime.now().strftime("%Y%m%d-%H%M"))
    
    model.export(MODEL_EXPORT_PATH + "latest")
    model.export(MODEL_EXPORT_PATH + "latest_min1")

    plot_training_history_loss(history)
    plot_training_history_mae(history)

def build_model():
    # The input should be a 1D array of 60 floats (-1, 0, 1)
    inputs = layers.Input(shape=(60,), dtype=tf.float32)

    # 3 dense layers
    x = layers.Dense(128, activation='relu')(inputs)
    x = layers.Dense(64, activation='relu')(x)
    x = layers.Dense(64, activation='relu')(x)
    x = layers.Dense(64, activation='relu')(x)
    x = layers.Dense(64, activation='relu')(x)
    x = layers.Dense(32, activation='relu')(x)

    # Output layer for regression (predict a single float value)
    outputs = layers.Dense(1)(x)

    # Build the model
    model = models.Model(inputs=inputs, outputs=outputs)
    return model

def build_dataset(path):
    # Load and prepare the dataset of Pylos Games from the JSON file in the data folder
    with open(path) as f:
        data = json.load(f)

    print(f"Processing {len(data)} games")

    boards = []
    scores = []

    for game_idx, game in enumerate(data):
        # Skip games that don't have both players in the selected players list, if it's not empty
        if SELECTED_PLAYERS and (game["lightPlayer"] not in SELECTED_PLAYERS or game["darkPlayer"] not in SELECTED_PLAYERS):
            continue

        winner = game["winner"]
        n_moves = len(game["boardHistory"])

        for i, board_as_long in enumerate(game["boardHistory"]):
            # Convert 64-bit integer to a binary string with 60 bits
            board_as_array = np.array([(board_as_long >> j) & 1 for j in range(60 - 1, -1, -1)], dtype=np.float32)
            discounted_score = winner * (DISCOUNT_FACTOR ** (n_moves - i))

            boards.append(board_as_array)
            scores.append(discounted_score)

        if game_idx % 1000 == 0:
            print(f"Processed game {game_idx}/{len(data)}")

    return np.array(boards, dtype=np.float32), np.array(scores, dtype=np.float32)

def plot_training_history_loss(history):
    epochs = range(1, len(history.history['loss']) + 1)

    # Plot loss
    plt.figure(figsize=(10, 6))
    plt.plot(epochs, history.history['loss'], label='Training Loss')
    plt.title('Training Loss Over Epochs')
    plt.xlabel('Epochs')
    plt.ylabel('Loss')
    plt.legend()
    plt.grid(True)
    plt.show()

def plot_training_history_mae(history):
    epochs = range(1, len(history.history['mae']) + 1)

    # Plot mae
    plt.figure(figsize=(10, 6))
    plt.plot(epochs, history.history['mae'], label='Training mae')
    plt.title('Training mae Over Epochs')
    plt.xlabel('Epochs')
    plt.ylabel('mae')
    plt.legend()
    plt.grid(True)
    plt.show()

if __name__ == "__main__":
    main()
