
import subprocess

def main():
    # Path to the directory containing the compiled Java classes and TensorFlow JARs
    classpath = r"C:\Users\Indra\KUL4_computer\Pylos\ai_pylos\ai_pylos\ai_pylos-main\pylos-ml\target\classes;" \
        r"C:\Users\Indra\.m2\repository\org\tensorflow\tensorflow-core-api\1.0.0-rc.2\tensorflow-core-api-1.0.0-rc.2.jar;"\
            r"C:\Users\Indra\.m2\repository\org\tensorflow\tensorflow-core-platform\1.0.0-rc.2\tensorflow-core-platform-1.0.0-rc.2.jar;"\
            r"C:\Users\Indra\.m2\repository\com\google\protobuf\protobuf-java\3.21.9\protobuf-java-3.21.9.jar;"\
            r"C:\Users\Indra\.m2\repository\com\google\code\gson\gson\2.11.0\gson-2.11.0.jar;"\
            r"C:\Users\Indra\.m2\repository\org\bytedeco\javacpp\1.5.10\javacpp-1.5.10.jar;"\

    # Fully qualified name of the Java class to run (including the package)
    java_class = "be.kuleuven.pylos.PylosMLReinforcementTrainer"

    # Java command to execute the class
    command = ['java', '-cp', classpath, java_class]

    try:
        # Run the command and wait for it to complete
        subprocess.run(command, check=True)
        print("Java class executed successfully!")
    except subprocess.CalledProcessError as e:
        print(f"Error executing the Java class: {e}")

if __name__ == "__main__":
    main()

