from bs4 import BeautifulSoup


with open("../dataset/xml/a01-000u.xml") as f:
    data = f.read()


bs_data = BeautifulSoup(data, "xml")
bs_data = bs_data.find_all("word")


for tag in bs_data:
    id = tag["id"]
    text = tag["text"]
    print(id, text)
    # bs_data[0]["height"]  # -> Positioning of the image
    # bs_data[0]["width"]  # -> Positioning of the image
    # bs_data[0]["x"]  # -> Positioning of the image
    # bs_data[0]["y"]  # -> Positioning of the image
