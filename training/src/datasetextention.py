import numpy as np
import os
import pickle

from bs4 import BeautifulSoup
from PIL import Image
from torch import tensor
from torch.utils.data import Dataset
from utility import distortion_free_resize


class DatasetExtention(Dataset):
    def __init__(self) -> None:
        self.labels = []
        self.data = []
        self.unique_words_dict = {}
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
                        if img.size[0] > 448:
                            continue
                        if img.size[1] > 256:
                            continue

                        self.labels.append(tag["text"])
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

            for label, _ in self.data:
                self.labels.append(label)

            data.close()

        self.label_helper()

    def __len__(self) -> int:
        return len(self.data)

    def __getitem__(self, n) -> tuple[np.array, np.array]:

        label = np.zeros([len(self.unique_words_dict), 1])
        label[self.unique_words_dict[self.data[n][0]]] = 1

        image = distortion_free_resize(
            np.asarray(Image.open(self.data[n][1]))) / 255

        return label, image

    def label_helper(self) -> None:
        for i, label in enumerate(set(self.labels)):
            self.unique_words_dict[label] = i
