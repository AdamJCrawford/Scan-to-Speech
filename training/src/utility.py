import numpy as np


def distortion_free_resize(image: np.array) -> np.array:
    h, w = 400, 2_000
    # Check the amount of padding needed to be done.
    pad_height = h - image.shape[0]
    pad_width = w - image.shape[1]

    # Only necessary if you want to do same amount of padding on both sides.
    if pad_height % 2 != 0:
        height = pad_height // 2
        pad_height_top = height + 1
        pad_height_bottom = height
    else:
        pad_height_top = pad_height_bottom = pad_height // 2

    if pad_width % 2 != 0:
        width = pad_width // 2
        pad_width_left = width + 1
        pad_width_right = width
    else:
        pad_width_left = pad_width_right = pad_width // 2

    return np.pad(image, [(pad_height_top, pad_height_bottom), (pad_width_left, pad_width_right)])
