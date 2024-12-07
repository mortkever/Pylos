from matplotlib import pyplot as plt
import tensorflow as tf
from tensorflow.keras import layers, models
import json
import numpy as np
import datetime
import os
import subprocess

#Split arrays or matrices into random train and test subsets.
#https://scikit-learn.org/stable/modules/generated/sklearn.model_selection.train_test_split.html
from sklearn.model_selection import train_test_split


 #DATASET_PATH = "resources/games/0.json"
#DATASET_PATH = "pylos-ml/src/main/training/resources/games/0.json"
#MODEL_EXPORT_PATH = "resources/models/"
DATASET_PATH = "pylos-ml/src/main/training/resources/games/1731625595073.json"
REINFORCE_DATASET_PATH = "pylos-ml/src/main/training/resources/games/reinforce.json"
MODEL_EXPORT_PATH = "resources/models/"
SELECTED_PLAYERS = []
DISCOUNT_FACTOR = 0.98
EPOCHS =  15 #10 #50 #100
BATCH_SIZE = 2048 #1024  #512 #
N_CORES = 8

os.environ["OMP_NUM_THREADS"] = str(N_CORES)
os.environ["TF_NUM_INTRAOP_THREADS"] = str(N_CORES)
os.environ["TF_NUM_INTEROP_THREADS"] = str(N_CORES)

def main():
    print("TensorFlow version:", tf.__version__)

    model = build_model()

    model.compile(optimizer='adam', loss='mean_squared_error')

    boards, scores = build_dataset(DATASET_PATH)
    boards_train, boards_test, scores_train, scores_test = train_test_split(boards, scores, test_size=0.2, random_state=42)

    print("# datapoints:", len(boards))

    #print the dataset
    print("boards:", boards)
    print("scores:", scores)

    history = model.fit(boards_train, scores_train, epochs=EPOCHS, batch_size=BATCH_SIZE)


    test_results = model.evaluate(boards_test, scores_test, verbose=1)

    model.export(MODEL_EXPORT_PATH + "latest_min1")
    model.export(MODEL_EXPORT_PATH + "latest")

    test_loss = test_results #[0]
    print(f"Training loss: {history.history['loss'][-1]}, Test loss: {test_loss}")

    while(history.history['loss'][-1] > 0.16 and test_loss > 0.175):   #latest loss value 
        #while(np.mean(history.history['loss']) > 0.20): #mean loss value is also possible

        print("New Loop started")
        #call PylosMLReinforcementTrainer
        current_dir = os.getcwd()
        jar_path = os.path.join(current_dir, 'pylos-ml', 'target', 'pylos-ml-1.0-SNAPSHOT.jar')
        command = ['java', '-jar', jar_path]
        model.export(MODEL_EXPORT_PATH + "latest_min1")
        
        try:
            # Run the command and wait for it to complete
            subprocess.run(command, check=True)
            print("Java class executed successfully!")
        except subprocess.CalledProcessError as e:
            print(f"Error executing the Java class: {e}")
        
        #model = build_model()
        #model.compile(optimizer='adam', loss='mean_squared_error')

        boards, scores = build_dataset(REINFORCE_DATASET_PATH)
        #nieuwe data, nieuwe split
        boards_train, boards_test, scores_train, scores_test = train_test_split(boards, scores, test_size=0.2, random_state=42)


        #https://stackoverflow.com/questions/39263002/calling-fit-multiple-times-in-keras
        history = model.fit( boards_train, scores_train, epochs=EPOCHS, batch_size=BATCH_SIZE)
        model.export(MODEL_EXPORT_PATH + "latest")

        test_results = model.evaluate(boards_test, scores_test, verbose=1)
        test_loss = test_results

        print(f"Training loss: {history.history['loss'][-1]}, Test loss: {test_loss}")


    print("Finished training")

    timestamp = datetime.datetime.now().strftime("%Y%m%d-%H%M")
    # Save the model as SavedModel, with date and time as the name
    model.export(MODEL_EXPORT_PATH + timestamp)
    
    model.export(MODEL_EXPORT_PATH + "latest")


    plot_training_history_loss(history, timestamp)
    #plot_training_history_mae(history)
    plot_training_vs_test(history, test_results, timestamp)

def build_model():
    # The input should be a 1D array of 60 floats (-1, 0, 1)
    inputs = layers.Input(shape=(60,), dtype=tf.float32)

    # 3 dense layers
    x = layers.Dense(128, activation='relu')(inputs)
    x = layers.Dense(128, activation='relu')(x)
    x = layers.Dense(64, activation='relu')(x)
    x = layers.Dense(64, activation='relu')(x)
    x = layers.Dense(32, activation='relu')(x)

    # x = layers.Dense(128, activation='relu')(inputs)
    # x = layers.Dense(64, activation='relu')(x)
    # x = layers.Dense(64, activation='relu')(x)
    # x = layers.Dense(64, activation='relu')(x)
    # x = layers.Dense(64, activation='relu')(x)
    # x = layers.Dense(32, activation='relu')(x)

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

def plot_training_history_loss(history, timestamp):
    epochs = range(1, len(history.history['loss']) + 1)

    plt.figure(figsize=(10, 6))
    plt.plot(epochs, history.history['loss'], label='Training Loss')
    plt.title('Training Loss Over Epochs')
    plt.xlabel('Epochs')
    plt.ylabel('Loss')
    plt.legend()
    plt.grid(True)

    plt.savefig('graphs/training_loss'+timestamp+'.png')
    print("Training loss plot saved as 'training_loss timestamp.png'")

def plot_training_vs_test(history, test_results, timestamp):
    epochs = range(1, len(history.history['loss']) + 1)

    # Plot loss
    plt.figure(figsize=(12, 6))
    plt.plot(epochs, history.history['loss'], label='Training Loss', color='blue')
    plt.axhline(y=test_results[0], color='blue', linestyle='--', label='Test Loss')
    
    # Plot MAE
    plt.plot(epochs, history.history['mae'], label='Training MAE', color='green')
    plt.axhline(y=test_results[1], color='green', linestyle='--', label='Test MAE')

    plt.title('Training vs Test Metrics')
    plt.xlabel('Epochs')
    plt.ylabel('Metric Value')
    plt.legend()
    plt.grid(True)

    
    graph_path = f'graphs/metrics_comparison_{timestamp}.png'
    plt.savefig(graph_path)
    print(f"Training vs Test metrics plot saved as '{graph_path}'")

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
