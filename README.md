# Rentals Management Web App (Django & SQL)

## Overview
This project is a full-stack web application built with Python and Django, designed to manage and analyze apartment rentals. It interfaces with a relational database to handle complex queries regarding tenants, landlords, and property details.

## Technical Highlights
* **Web Framework:** Built using Django, demonstrating a solid understanding of the MVT (Model-View-Template) architectural pattern.
* **Database Integration:** Features custom SQL views and complex queries to extract advanced analytics (e.g., identifying "Minimalist Renters" or calculating the distribution of landlords across cities).
* **Frontend Interfaces:** Includes HTML templates equipped with Django template tags to dynamically render database query results and user search forms.
* **Data Modeling:** Utilizes Django's ORM (Object-Relational Mapping) to define robust database schemas for Apartments, Owners, and Rentals.

## Core Components
* `Rentals_App/models.py`: Defines the data models and database relationships.
* `Rentals_App/templates/`: Contains the frontend HTML views and routing interfaces.
* `sql/queries_views.sql`: The raw SQL script used to generate complex database views for the application's analytical features.
