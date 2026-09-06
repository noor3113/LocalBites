# Local Bites 🍲

A hyperlocal, community-driven Android application for discovering unmapped street food stalls, thelas, and small food vendors that aren't listed on mainstream platforms like Google Maps.

## Overview

Local Bites lets users find and add small, informal food vendors (street carts, chai stalls, snack thelas) that are usually missed by big delivery/discovery apps. The app is entirely crowd-sourced — any user can add a stall with its location, photo, and details, and the community keeps information (like open/closed status) up to date.

## Features

- **Interactive Map & List View** — Discover nearby stalls on a live Google Map or browse them as a searchable list.
- **Add a Stall** — Users can add unmapped vendors by dropping a pin on an embedded map, adding a name, category, price range, landmark, and photo.
- **Category Filters** — Filter stalls by Tea & Breakfast, Fast Food, Snacks, Juice, Desi Food, or a custom "Other" category.
- **Live Open/Closed Status** — Any user can update whether a stall is currently open, keeping information fresh.
- **Ratings & Reviews** — Leave a star rating and written review for any stall.
- **Delete Listings** — Remove outdated or incorrect stall entries.
- **AI Assistant** — An in-app guide that answers questions about the app and helps users find specific types of food.
- **Dark Themed UI** — A warm, amber-accented dark interface built with Jetpack Compose.

## Tech Stack

- **Language:** Kotlin
- **UI Framework:** Jetpack Compose (Material 3)
- **Architecture:** Repository pattern with Kotlin Flow
- **Local Storage:** Room Database
- **Maps:** Google Maps SDK for Android (Maps Compose)
- **Location:** Android FusedLocationProvider / Location Services
- **Image Handling:** Coil, CameraX-based photo capture

- ## Screenshots

<p float="left">
  <img src="screenshots/Home%20page.jpeg" width="200" />
  <img src="screenshots/Add%20Stall%20Screen.jpeg" width="200" />
  <img src="screenshots/Chatbot.jpeg" width="200" />
  <img src="screenshots/About.jpeg" width="200" />
</p>

## Purpose

This project was built to demonstrate a full-featured, community-driven Android application — covering CRUD operations, real-time location services, map integration, and local data persistence — as part of an academic portfolio project.
