# FrontEnd Documentation

## Libraries
## File Structure
## Site Overview

In this section, different components and pages of the front end will be discussed as the front end plays an important role in our project. The components will be discussed according to the workflow i.e in the order how we will use the pages to get the final results.

### Toolbar

![Toolbar](images/toolbar.png)

The toolbar is the main navigation element of the site. It is available at all pages which makes the navigation easy. Since the project makes use of the single page application, the toolbar is not required to be added in each html page and implemented only at one place.

The toolbar has two options represented with following icons.


 The first icon is ![Toolbar](images/ic_menu_black_24dp_1x.png). Once this menu button is clicked, the following side bar will appear.

 ![Navigation](images/navigation.png)

 It has the following options

 - Map View
 - View Projects
 - Import Data
 - Manage Data

 The following sections will explain each of these menu options and submenus under them with respect to the workflow.

### Import Data

This section is dedicated for the import options. The data needs to be fed to the system to obtain the results. Once you click on the Import Data option from the navigation side bar, a secondary toolbar will appear below the main toolbar providing different import options. The second toolbar is shown below.

![Import Toolbar](images/import.png)

#### Buildings

In this page, you can add building related data such as name, number of floors and dimensions of the floor map image.

![Building Data](images/addBuilding1.png)

It also has an option to add the cordinations either with *center and rotation* or add with latitudes and longitudes. Below images show these options.

![Building Data with Latitudes and Longitudes](images/addBuilding2.png)

To add the cordinates with center and rotation the radio button has to be turned on.

![Building Data with Center and Rotation](images/addBuilding3.png)

Even though these parts are explained separately, they are present in the single page.

## Validation
