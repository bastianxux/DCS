import json
import random

def add_labels_to_points(input_file, output_file):
    def assign_label(point):
        random_factor = random.uniform(-10, 10)
        if point["x"] + random_factor > 50 and point["y"] + random_factor > 50:
            return "A"
        elif point["x"] + random_factor <= 50 and point["y"] + random_factor <= 50:
            return "B"
        else:
            return "C"

    with open(input_file, "r") as f:
        data = [json.loads(line) for line in f]

    assigned_data = []
    for point in data:
        point["label"] = assign_label(point)
        assigned_data.append(json.dumps(point))

    with open(output_file, "w") as json_file:
        json_file.write("\n".join(assigned_data))

    print(f"成功为 {len(data)} 个点添加标签，并保存到 {output_file}")

if __name__ == "__main__":
    input_file = "../data/points.json"
    output_file = "../data/points_with_labels.json"
    add_labels_to_points(input_file, output_file)