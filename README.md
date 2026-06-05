# CatApp

## Description

CatApp is an Android application developed using Kotlin that uses
the [Cat API](https://thecatapi.com/) to show information about various cat breeds.

## Setup

To set up the project, follow these steps:

1. Clone the repository.
2. Open the project in Android Studio.
3. Sync the project with Gradle files.
4. Add the Cat API key to the `secrets.properties` file (create it if it doesn't exist) as
   follows:
   ```
   API_KEY=YOUR_API_KEY
   ```
   This is loaded by `core/data/build.gradle.kts` and exposed as `BuildConfig.CAT_API_KEY`.

## Unit Tests

To run unit tests from the terminal, use the Gradle Wrapper command `./gradlew`. The command to run
all unit tests is:

```bash
./gradlew test
```

## Running Tests

To run Android UI tests (androidTests) from the terminal, use the Gradle Wrapper
command `./gradlew`. The command to run all UI tests is:

```bash
./gradlew connectedAndroidTest
```

## Features

The application has the following features:

- A screen with a list of cat breeds, displaying:
    - Cat image
    - Breed name
- The cat breeds screen contains a search bar to filter the list by breed name.
- The cat breeds screen contains a button to mark the breed as favourite.
- A favourites tab that shows the breeds marked as favourites, displaying:
    - The average lifespan of all the favourite breeds (using the lower value in the range).
- A screen with a detailed view of a breed, displaying:
    - Breed Name
    - Origin
    - Temperament
    - Description
    - A button to add/remove the breed from the favourites.
- Navigation between the different screens is managed by Navigation 3.
- Pressing on one of the list elements (in any screen) opens the detailed view of a breed.

## Technical Requirements

The application meets the following technical requirements:

- MVVM architecture
- Usage of Jetpack Compose for UI building
- Unit test coverage
- Offline functionality (using Room for data persistence)
- Follows the Single Source of Truth principle
- Error Handling
- Pagination for the list of cat breeds
- Modular design
- End-to-end smoke test

## Decisions

The following decisions were made during the development of the application:

- Tech stack used:
    - Jetpack Compose for UI building
    - Navigation 3 for navigation
    - Coil 3 for loading images
    - Paging 3 for pagination
    - Room for data persistence
    - Retrofit with OkHttp for network calls
    - Kotlinx Serialization for JSON parsing
    - Hilt for dependency injection
    - Coroutines for asynchronous calls
    - JUnit for unit tests
    - Mockito-Kotlin for mocking objects in unit tests

- Modular structure:
    - `app`: Contains the application initialization, the main activity and the app navigation
      logic.
    - `core:data`: Contains the data layer, including the network and database as well as the data
      modules used.
    - `core:domain`: Contains the domain layer.
    - `core:ui`: Contains reusable UI components (`CatImage`, `PagingContent`,
      `StateComponents`) and theme (`Color`, `Dimens`, `Theme`, `Type`).
    - `feature:breeds`: Contains the UI and logic for the cat breeds list.
    - `feature:details`: Contains the UI and logic for the cat breed details screen.
    - `feature:favorites`: Contains the UI and logic for the favourites screen.

This modular structure allows for a clear separation of concerns, and makes it easier to add new
features to the application.