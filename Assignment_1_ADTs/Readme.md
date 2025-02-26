# TaskSchedulerProject

## Overview
The **TaskSchedulerProject** is a Java-based multi-threaded task scheduler that utilizes different **Abstract Data Types (ADT)** such as **Queue, Stack, and Deque** to efficiently schedule and process tasks based on priority levels. The project is implemented using **Maven** for dependency management and supports automated testing with **JUnit**.

## Features
- Implements **High**, **Medium**, and **Low** priority tasks using **Stack, Deque, and Queue** respectively.
- Multi-threaded execution using **Java's ExecutorService**.
- Records task processing **metrics** such as **wait time, turnaround time, and throughput**.
- Stores results in a **CSV file** for further analysis.
- **JUnit tests** for validating the functionality.

## Project Structure
```
TaskSchedulerProject/
│── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/taskscheduler/
│   │   │       ├── TaskSchedulerExperiment.java
│   │   │       ├── Task.java
│   │   │       ├── TaskScheduler.java
│   │   │       ├── Worker.java
│   ├── test/
│   │   ├── java/
│   │   │   └── com/taskscheduler/
│   │   │       ├── TaskTest.java
│── output/
│   ├── runs.csv  # Stores task execution results
│── pom.xml       # Maven configuration
│── README.md     # Project Documentation
```

## Installation & Setup
### Prerequisites
Ensure you have the following installed:
- **Java 8+**
- **Maven**
- **Git** (optional, for version control)

### Clone the Repository
```sh
git clone https://github.com/your-username/TaskSchedulerProject.git
cd TaskSchedulerProject
```

### Build the Project
Run the following command to build the project:
```sh
mvn clean install
```

### Running the Application
To execute the **TaskSchedulerExperiment**, use:
```sh
mvn exec:java -Dexec.mainClass="com.taskscheduler.TaskSchedulerExperiment"
```
Alternatively, you can run it directly:
```sh
java -cp target/TaskSchedulerProject-1.0-SNAPSHOT.jar com.taskscheduler.TaskSchedulerExperiment
```

### Running Tests
To execute all unit tests:
```sh
mvn test
```

## Output
- **Metrics such as wait time, turnaround time, and throughput** will be displayed in the terminal.
- The results will be stored in `output/runs.csv`.

## Contributing
1. Fork the repository.
2. Create a new branch: `git checkout -b feature-branch`.
3. Make your changes and commit: `git commit -m "Your changes"`.
4. Push to the branch: `git push origin feature-branch`.
5. Open a Pull Request.

## License
This project is licensed under the **MIT License**.

