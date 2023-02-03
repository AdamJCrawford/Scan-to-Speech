import multiprocessing
import numpy as np
import os

from bs4 import BeautifulSoup
import matplotlib.pyplot as plt
from PIL import Image
from utility import distortion_free_resize


# NUMBER_OF_IMAGES = 115_320
# LARGEST_DIMENSIONS = (342, 1_934)


def get_images(directory: str) -> list[np.array]:
    images = []
    for image in os.listdir(directory):
        try:
            img = np.asarray(Image.open(directory + image))
        except:
            os.remove(directory + image)
            continue
        images.append(distortion_free_resize(img))
    return images


def get_training_data_process(files: list, queue: multiprocessing.Queue) -> iter(str, np.array):
    # TODO: stop using lists and switch over to np arrays

    label_path = "../dataset/xml/"
    img_path = "../dataset/words/"

    labels = []
    images = []

    for file in files:
        with open(label_path + file) as f:
            data = f.read()

        bs_data = BeautifulSoup(data, "xml").find_all("word")

        for i, tag in enumerate(bs_data):
            labels.append(tag["text"])

            if i == 0:
                # file name of image
                curr_id = tag["id"]
                # Index of the first dash in the file name
                first_dash_index = curr_id.index('-')

                # x
                first_dir = curr_id[:first_dash_index]
                # y
                second_dir = curr_id[:curr_id.index('-', first_dash_index + 1)]
                # ../dataset/words/x/y
                whole_directory = img_path + first_dir + '/' + second_dir + '/'

                images.extend(get_images(whole_directory))
    queue.put([labels, images])


def get_training_data() -> tuple[list[str], list[np.array]]:

    cpus = multiprocessing.cpu_count()
    queue = multiprocessing.Queue()

    directory = "../dataset/xml/"

    xml_files = [name for name in os.listdir(
        directory)if os.path.isfile(os.path.join(directory, name))]

    num_files = len(xml_files)

    files_per_core, remainder = divmod(num_files, cpus)

    processes = []
    for i in range(1):
        first_index = i * files_per_core
        second_index = (i + 1) * files_per_core if i != cpus - 1 else 2

        process = multiprocessing.Process(
            target=get_training_data_process, args=[xml_files[first_index:second_index], queue])

        processes.append(process)
        process.start()
    for proc in processes:
        proc.join()
    labels, images = [], []
    for _ in range(1):
        results = queue.get()
        print("Got something")
        print(results)
        labels.extend(results[0])
        images.extend(results[1])
    return labels, images

# for label, image in get_training_data_process():
#     print(label)
#     plt.imshow(image)
#     plt.show()
