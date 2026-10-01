# Data Visualization System

A Java 25 desktop application that loads structured data from CSV files and presents it through interactive bar and pie charts. Built with Java Swing and JFreeChart, it includes CSV validation, record selection, PNG export, and automated unit tests.

## Table of Contents

- [Features](#features)
- [Technologies Used](#technologies-used)
- [Project Structure](#project-structure)
- [CSV Format](#csv-format)
- [Architecture](#architecture)
- [Getting Started](#getting-started)
- [Using the Application](#using-the-application)
- [Chart Types](#chart-types)
- [Validation](#validation)
- [Testing](#testing)
- [Future Improvements](#future-improvements)
- [Author](#author)

## Features

- Import CSV datasets through a file chooser
- Parse and validate CSV data, including quoted values, commas inside quoted fields, and multiline quoted fields
- Display validation errors with row and column context
- Select individual records from the loaded dataset
- Generate interactive bar charts with measurement value labels
- Generate pie charts with percentage labels for valid non-negative measurements
- Interactive chart tooltips and mouse-wheel zoom
- Export the current chart as a PNG image
- No hardcoded local file paths
- Modular separation of responsibilities
- Automated unit testing with JUnit 5

## Technologies Used

| Technology | Purpose |
|---|---|
| Java 25 | Core language |
| Java Swing | Desktop GUI |
| JFreeChart 1.5.6 | Chart rendering |
| Maven | Build and dependency management |
| JUnit 5 | Unit testing |

## Project Structure

```text
Data Visualization System/
├── src/
│   ├── main/
│   │   └── java/
│   │       ├── ChartGenerator.java
│   │       ├── CsvReader.java
│   │       ├── DataVisualizationApp.java
│   │       ├── StudentRecord.java
│   │       └── StudentsData.java
│   └── test/
│       └── java/
│           ├── ChartGeneratorTest.java
│           ├── CsvReaderTest.java
│           └── StudentRecordTest.java
├── Students.csv
├── pom.xml
├── README.md
└── .gitignore
```

## CSV Format

The CSV file must contain a record ID, a record name, and one or more numeric measurements.

**Example:**

```csv
SL NO.,NAME,MATHS,SCIENCE,ENGLISH
1,Alice,85,88,90
2,Bob,76,85,78
3,Charlie,92,83,91
4,Shrey,99,84,95
5,Ravi,72,80,74
```

**Requirements:**

- The first column is the record ID.
- The second column is the record name.
- All remaining columns are numeric measurements.
- Measurement headers must be unique.
- Measurement values must be valid, finite numbers.
- Missing or invalid values are reported with the relevant row and column.

## Architecture

Each component has a single, clearly defined responsibility.

| Class | Responsibility |
|---|---|
| `StudentRecord` | Represents an individual data record: ID, name, and measurement values. |
| `CsvReader` | Reads and parses CSV files, handles quoted values, validates headers and measurements, and reports invalid or missing data. |
| `ChartGenerator` | Generates bar and pie charts, and validates measurement values before creating pie charts. |
| `DataVisualizationApp` | Swing GUI: file selection, record selection, chart selection, chart display, chart export, and status information. |
| `StudentsData` | Application entry point; launches the desktop application. |

## Getting Started

### Prerequisites

- JDK 25 or later
- Apache Maven

Verify your installation:

```bash
java -version
mvn -version
```

### Build

From the directory containing `pom.xml`:

```bash
mvn clean compile
```

### Run

```bash
mvn exec:java
```

The Data Visualization System window will open.

## Using the Application

1. Click **Open CSV**.
2. Select a compatible CSV file.
3. Choose a record from the **Record** dropdown.
4. Choose **Bar chart** or **Pie chart**.
5. View the generated visualization, and use tooltips and mouse-wheel zoom to explore it.
6. Click **Export chart** to save the current chart as a PNG image.

## Chart Types

### Bar Chart

Displays the measurements of the selected record as individual bars. It includes:

- Measurement categories
- Numeric value labels
- Axis labels and gridlines
- A record-specific title

```text
Maths     ██████████████████  92
Science   ████████████████    83
English   █████████████████   91
```

### Pie Chart

Displays the relative contribution of each measurement. It includes:

- Measurement labels
- Percentage values
- A legend
- A record-specific title

Pie charts are created only when all measurement values are non-negative and their total is greater than zero.

### Chart Export

The currently displayed chart can be exported as a PNG image, so visualizations can be saved and reused outside the application.

## Validation

Data is validated before it is processed. Pie charts, for example, reject:

- Negative measurements
- A total measurement value of zero
- Non-finite values such as `NaN`
- Invalid numeric data

This prevents invalid datasets from producing misleading visualizations.

## Testing

The project uses JUnit 5. Run the full test suite with:

```bash
mvn clean test
```

**Current coverage includes:**

- Chart generation
- Bar chart data validation
- Pie chart validation
- CSV parsing and validation
- Student record behavior
- Invalid and edge-case input handling

**Latest result:**

```text
Tests run: 15, Failures: 0, Errors: 0, Skipped: 0

BUILD SUCCESS
```

## Future Improvements

- Additional chart types
- Multiple dataset comparison
- Additional export formats
- Improved dataset filtering
- Summary statistics
- Custom chart configuration
- Expanded automated test coverage

## Author

**Rummana Shirin Ali**
B.Tech, Computer Science and Engineering