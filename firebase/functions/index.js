const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp(functions.config().firebase);

// // Create and Deploy Your First Cloud Functions
// // https://firebase.google.com/docs/functions/write-firebase-functions

exports.newPendingRequest = functions.database.ref('pending_requests/{playerId}/{requestId}').onWrite(event => {
    let requestData = event.data.val();
    console.log(requestData);
    if (typeof requestData == 'undefined' || requestData == null) {
        return requestData;
    }
    if (!event.data.previous.exists()) {
		// Do things here if project didn't exists before
	}
	console.log(requestData.senderName);
	let msg = `You gotta pending request from ${requestData.senderName}!`;

    // If the request ID == player ID then this request was made by the user and we have to send a
    // notification to the league managers.
    defer = null;
    if (requestData.senderId == requestData.playerId) {
        defer = loadLeagueManagers(requestData.league.league_id).then(userIds => {
            console.log("Got managers should send notification to " + userIds.length + " users");
            for (var index in userIds) {
                console.log("send notification to ID: " + userIds[index]);
                loadUserForNotificationDebug(userIds[index]).then(user => {
                    let tokens = [];
                    if (typeof user.pushToken != 'undefined') {
                        tokens.push(user.pushToken);
                    }

                    let payload = {
                        notification: {
                            title: 'Firebase Notification',
                            body: msg,
                            sound: 'default',
                            badge: '1'
                        }, data: {
                            content: JSON.stringify(requestData)
                        }
                    };
                    admin.messaging().sendToDevice(tokens, payload);
                });
            }
            return userIds;
        });
    } else {
	    defer = loadUserForNotificationDebug(requestData.playerId).then(user => {
            let tokens = [];
            if (typeof user.pushToken != 'undefined') {
                tokens.push(user.pushToken);
            }

            let payload = {
                notification: {
                    title: 'Firebase Notification',
                    body: msg,
                    sound: 'default',
                    badge: '1'
                }, data: {
                    content: JSON.stringify(requestData)
                }
            };
            return admin.messaging().sendToDevice(tokens, payload);
        });
    }
});

function loadUserForNotificationDebug(userId) {
    let dbRef = admin.database().ref('players/'+userId);
    let defer = new Promise((resolve, reject) => {
        dbRef.once('value', (snap) => {
            let data = snap.val();
            resolve(data);
        }, (err) => {
            reject(err);
        });
    });
    return defer;
}

function loadLeagueManagers(leagueId) {
    let dbRef = admin.database().ref('leagues/'+leagueId);
    let defer = new Promise((resolve, reject) => {
        dbRef.once('value', (snap) => {
            let data = snap.val();
            let ids = [];
            map = data.managerIds
            for (var key in map){
                if (map[key] == true){
                    console.log("League Manager ID: "+ key);
                    ids.push(key);
                }
            }
            resolve(ids);
        }, (err) => {
            reject(err);
        });
    });
    return defer;
}

function loadUsers() {
    let dbRef = admin.database().ref('players');
    let defer = new Promise((resolve, reject) => {
        dbRef.once('value', (snap) => {
            let data = snap.val();
            let users = [];
            for (var property in data) {
                users.push(data[property]);
            }
            resolve(users);
        }, (err) => {
            reject(err);
        });
    });
    return defer;
}