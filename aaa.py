import os
from pathlib import Path

def merge_code_to_txt(input_dir, output_file):
    """将目录中的代码文件合并到单个txt中"""
    allowed_ext = ('.java', '.xml')
    separator = '\n' + '=' * 80 + '\n'  # 文件分隔线
    
    with open(output_file, 'w', encoding='utf-8') as out_f:
        for root, dirs, files in os.walk(input_dir):
            for file in files:
                if file.lower().endswith(allowed_ext):
                    file_path = Path(root) / file
                    try:
                        # 写入文件名
                        out_f.write(f"\n{separator}")
                        out_f.write(f"File: {file_path}\n")
                        out_f.write(f"{separator}\n")
                        
                        # 写入文件内容
                        with open(file_path, 'r', encoding='utf-8') as in_f:
                            out_f.write(in_f.read())
                            out_f.write('\n')  # 文件末尾换行
                            
                    except Exception as e:
                        print(f"Error processing {file_path}: {str(e)}")

if __name__ == "__main__":
    # 配置路径（根据实际情况修改）
    source_dir = "app/src/main"    # 源代码目录
    output_txt = "merged_code.txt" # 输出文件
    
    print(f"Merging code files from {source_dir}...")
    merge_code_to_txt(source_dir, output_txt)
    print(f"Merge completed! Output file: {output_txt}")