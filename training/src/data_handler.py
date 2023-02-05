import numpy as np
import os
import pickle

from bs4 import BeautifulSoup
from PIL import Image
from utility import distortion_free_resize


class DataLoader():
    def __init__(self, batch_size) -> None:
        self.curr_index = 0
        self.batch_size = batch_size
        self.data = []
        self.out_label_path = "../dataset/xml/"
        self.outer_img_path = "../dataset/words/"

        if not os.path.exists("data"):
            # If there is not a pickle file, have to create it

            for file in os.listdir(self.out_label_path):
                with open(self.out_label_path + file) as f:
                    data = f.read()

                bs_data = BeautifulSoup(data, "xml").find_all("word")

                for tag in bs_data:
                    # File name of image
                    curr_id = tag["id"]
                    # Index of the first dash in the file name
                    first_dash_index = curr_id.index('-')

                    # x
                    first_dir = curr_id[:first_dash_index]
                    # y
                    second_dir = curr_id[:curr_id.index(
                        '-', first_dash_index + 1)]
                    # ../dataset/words/x/y
                    whole_directory = self.outer_img_path + first_dir + \
                        '/' + second_dir + '/' + curr_id + ".png"

                    try:
                        # Make sure that the image can be opened
                        img = Image.open(whole_directory)
                        self.data.append([tag["text"], whole_directory])
                    except:
                        try:
                            # Remove the image if it exists and it can't be loaded
                            os.remove(whole_directory)
                        except:
                            # If the image has already been removed, continue the loop
                            continue

            # create pickle file
            data = open('data', 'ab')

            pickle.dump(self.data, data)

            data.close()

        else:
            # If the pickle was already created, load it
            data = open("data", "rb")

            self.data = pickle.load(data)

            data.close()

    def get_next_batch(self) -> iter(str, np.array):
        # Use min() to ensure that we don't use an invalid index
        cur_range = range(self.curr_index, min(
            self.curr_index + self.batch_size, len(self.data)))

        curr_labels = [self.data[i][0] for i in cur_range]
        curr_images = [distortion_free_resize(np.asarray(
            Image.open(self.data[i][1])) / 255) for i in cur_range]

        self.curr_index += self.batch_size
        return zip(curr_labels, curr_images)
