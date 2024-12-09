import json
import random

def generate_json(file_path, num_points):
    data = []
    for _ in range(num_points):
        point = {
            "x": round(random.uniform(0, 100), 2), # 2位小数
            "y": round(random.uniform(0, 100), 2)
        }
        # data.append(point)
        data.append(json.dumps(point))

    # with open(file_path, "w") as json_file:
    #     json.dump(data, json_file, indent=4)
    with open(file_path, "w") as json_file:
        json_file.write("\n".join(data))

    print(f"成功生成 {num_points} 个随机点并保存到 {file_path}")

if __name__ == "__main__":
    output_file = "../data/points.json"
    num_points = 100000
    generate_json(output_file, num_points)