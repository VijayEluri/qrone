
user.logout();

document.write({
	"status" : "OK",
	"user" : {
		"name" : user.name,
		"id" : user.id,
		"deviceId" : user.deviceId,
		"store" : user.store
	}
});