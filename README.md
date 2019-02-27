# Campsite Backend Challenge

# Notes

## Dates for booking/checking availability

From/To dates params must be UNIX epoch milliseconds and they are going to be handled with UTC timezone.

## The following methods are exposed through a REST API:

#### Book the campsite 

[POST] /api/campsite/v1/reservations

Payload example:
```json
{
	"from": 1551372945000,
	"to":1551545745000,
	"guestFirstName": "Esteban",
	"guestLastName": "Fernandez",
	"guestEmail": "esteban@upgrade.com"
}
```
#### Get available intervals for booking

[GET] /api/campsite/v1/availability

#### Update a reservation

[PATCH] /api/campsite/v1/reservations/{reservationId}
```json
{
	"from": 1551372945000,
	"to":1551545745000
}
```
#### Cancel a reservation

[POST] /api/campsite/v1/reservations/{reservationId}/cancel

#### Read all reservations (method added just for read and check created reservations, it was not required in specifications within PDF)

[GET] /api/campsite/v1/reservations

