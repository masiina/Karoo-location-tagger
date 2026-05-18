Application for Hammerhead KAroo 3 bicycle computer.

Application purpose is to:

Functional requirements:
- Custom field that is a button which opens app view when pressed
- app view:
    - Two tabs: 
        new entry:
        - LOV named "type", values: MTB, road
        - LOV named "potential": 1, 2, 3
        - Save button that stores GPS location, and above details to local storage.
        Saved POIs:
        - list view of stored POIs with information of:
            - distance to item from current GPS location
            - direction to item from current user direction
            - button per item that opens "drop pin" with stored GPS location details map view and allow user to start navigating to target
            - Possibility to remove stored POIs 

Technical requirements:
- offline, no network connection needed
- Local data storage for stored items