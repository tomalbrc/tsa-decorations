import os
import shutil
import re
import sys

# List of names to replace
replacement_names = [
    "acacia", 
    "birch", 
    "darkoak", 
    "oak", 
    "jungle", 
    "mangrove", 
    "cherry", 
    "warped", 
    "crimson", 
    "bamboo", 
    "spruce"
]

def copy_files_with_replacement(original_name, target_directory):
    # Check which name is currently in the original filename
    for name in replacement_names:
        if name in original_name:
            original_file_path = os.path.join(target_directory, original_name)

            # If the original file exists
            if os.path.isfile(original_file_path):
                # Loop through each replacement name
                for replacement in replacement_names:
                    if replacement != name:  # Skip the original name
                        # Create new name by replacing the found name with the new one
                        new_file_name = original_name.replace(name, replacement)
                        new_file_path = os.path.join(target_directory, new_file_name)

                        # Copy the original file to the new file path
                        shutil.copy2(original_file_path, new_file_path)
                        print(f"Copied '{original_file_path}' to '{new_file_path}'")
                        
                        # Replace occurrences inside the new file if it's a txt or json file
                        if new_file_name.endswith(('.txt', '.json')):
                            replace_in_file(new_file_path, name, replacement)
            else:
                print(f"File '{original_file_path}' not found.")
            break

def replace_in_file(file_path, old_name, new_name):
    """Replace occurrences of old_name with new_name in the given file."""
    # Read the original content
    with open(file_path, 'r', encoding='utf-8') as file:
        content = file.read()

    # Replace occurrences
    updated_content = content.replace(old_name, new_name)

    # Write the updated content back to the file
    with open(file_path, 'w', encoding='utf-8') as file:
        file.write(updated_content)
    print(f"Replaced occurrences of '{old_name}' with '{new_name}' in '{file_path}'")

def search_directory_for_files(target_directory, search_name):
    for root, _, files in os.walk(target_directory):
        for file in files:
            # Get the base name without extension
            base_name, _ = os.path.splitext(file)
            # Check if the base name matches the search name
            if base_name == search_name:
                copy_files_with_replacement(file, root)

if __name__ == "__main__":
    # Check for command line arguments
    if len(sys.argv) > 2:
        input_name = sys.argv[1]
        directory_to_search = sys.argv[2]
    else:
        # Input: the name to search for
        input_name = input("Enter the name to search for (e.g., small_oak_table): ")
        directory_to_search = input("Enter the directory to search in: ")

    search_directory_for_files(directory_to_search, input_name)
