from data_handler import DataLoader
import matplotlib.pyplot as plt

# NUMBER_OF_IMAGES = 115_316
# LARGEST_DIMENSIONS = (342, 1_934)


def main() -> None:
    batch_size = 32
    testing = DataLoader(batch_size)
    for label, array in testing.get_next_batch():
        print(label)
        plt.imshow(array)
        plt.show()


if __name__ == "__main__":
    main()
