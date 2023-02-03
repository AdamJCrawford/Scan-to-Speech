from data_handler import get_training_data


def main() -> None:
    labels, images = get_training_data()
    print(len(labels), len(images))


if __name__ == "__main__":
    main()
