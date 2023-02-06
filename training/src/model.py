from datasetextention import DatasetExtention
import matplotlib.pyplot as plt

import torch
import torch.nn as nn
import torch.nn.functional as F
from torch.utils.data import Dataset, DataLoader
import torch.utils.data as data_utils

# NUMBER_OF_IMAGES = 115_316
# LARGEST_DIMENSIONS = (342, 1_934)


# class ConvNet(nn.Module):
#     def __init__(self, final_length) -> None:
#         super(ConvNet, self).__init__()
#         self.conv1 = nn.Conv2d(2, 64, 5)
#         self.pool = nn.MaxPool2d(2, 2)
#         self.conv2 = nn.Conv2d(64, 16, 5)
#         self.fc1 = nn.Linear(400, 120)  # Layer takes 16 * 5 * 5 inputs
#         self.fc2 = nn.Linear(120, 84)
#         self.fc3 = nn.Linear(84, final_length)
#
#     def forward(self, x) -> int:
#         x = self.pool(F.relu(self.conv1(x)))
#         x = self.pool(F.relu(self.conv2(x)))
#         x = x.view(-1, 400)
#         x = F.relu(self.fc1(x))
#         x = F.relu(self.fc2(x))
#         x = self.fc3(x)
#         return x


def main() -> None:
    device = torch.device('cuda' if torch.cuda.is_available() else 'cpu')

    num_epochs = 5
    batch_size = 32
    learning_rate = 1e-3

    dataset = DatasetExtention()
    data_loader = DataLoader(dataset, batch_size, shuffle=True)

    # model = ConvNet(len(data_loader)).to(device)
    # optimizer = torch.optim.Adam(
    #     model.parameters(), lr=learning_rate, weight_decay=1e-4)
    # loss_function = nn.CrossEntropyLoss()
    #
    # for _ in range(num_epochs):
    #     model.train()
    #     for i, (labels, images) in enumerate(data_loader):
    #         labels = labels.to(device).float()
    #         images = images.to(device).float()
    #
    #         outputs = model(images)
    #         print(outputs)
    #         loss = loss_function(outputs, labels)
    #
    #         optimizer.zero_grad()
    #         loss.backward()
    #         optimizer.step()
    #         exit()
    #     model.eval()


if __name__ == "__main__":
    main()
