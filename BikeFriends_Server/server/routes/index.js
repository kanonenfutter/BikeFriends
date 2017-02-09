var express = require('express');
var router = express.Router();
var mongoDB = require('mongoskin');
var BSON = require('mongodb').BSONPure;
var db = mongoDB.db('mongodb://localhost/mydb?auto_reconnect=true', {
    safe: true
});
var http = require('http');

var register = require('functions/register');
var login = require('functions/login');

db.bind('profiles');

var profilesCollection = db.profiles;

db.bind('events');

var eventsCollection = db.events;


router.get('/', function(req, res, next) {
    res.render('index', {
        title: 'Express'
    });
});


/* GET home page. */
//TODO: Do Sanitization!
router.get('/profiles', function(req, res, next) {
    profilesCollection.findItems(function(error, result) {
        if (error)
            next(error);
        else {
            console.log(result);
            res.writeHead(200, {
                'Content-Type': 'application/json'
            });
            res.end(JSON.stringify(result));
        }
    });
});

/* GET Matches */
router.get('/profiles/:id/matches', function(req, res) {
    // Update. Wenn nicht existent, insert.
    var user;
    profilesCollection.findOne({_id:mongoDB.helper.toObjectID(req.params.id)},function(error, result){
        if (error){
            res.json({response: "Profil nicht gefunden."});
        }else{
            user = result;
            console.log("ergebniss " + result.residence);
            console.log("user: " + user.residence);
            console.log("result: " + result.residence);

            //matching kriterien festlegen
            // Matchingkriterien: Gemeinsame Radsportart, Durchschnitliche Geschwindigkeit ist min. 2km/h bzw. max. 2km/h groesser als medianSpeed
            //var matching_sports = req.body.bikesports[0];
            var location = user.residence;
            if (user.bikesports!==null) {
                var medianSpeed = req.body.averageSpeed;
                var minSpeed = medianSpeed - 2;
                if (minSpeed > 0) {
                    minSpeed = 0;
                }
                var maxSpeed = medianSpeed + 2;
            }
            var resultsArray;
            profilesCollection.find(
        /*    {
                $and: [{
                    bikesports: user.bikesports
                }, {
                    averageSpeed: {
                        $gte: minSpeed
                    }
                }, {
                    averageSpeed: {
                        $lte: maxSpeed
                    }
                }, ]
            }*/
            {
            residence: user.residence
            }

            ).toArray(function(error, results) {
                if (error)
                    next(error);
                else {
                    console.log(results);
                    resultsArray = results;
                    var jsonObject = {
                        results: resultsArray
                    };
                    res.writeHead(200, {
                        'Content-Type': 'application/json'
                    });
                    res.end(JSON.stringify(jsonObject));
                }
            });
        }
    });

});



router.get('/profiles/:id', function(req, res, error) {
    console.log("GET: " + JSON.stringify(req.url));
    console.log("param: token:" + req.params.id);
    //DEFECTIVE: var obj_id = BSON.ObjectID.createFromHexString(req.params.id);
    //find ressource 'profile :id' db.ObjectID.createFromHexString(req.params.id)
    console.log("test");
    profilesCollection.find({
        _id: mongoDB.helper.toObjectID(req.params.id)
    }).toArray(function(error, result) {
        //console.log(result);
        if (error)
            next(error);
        else {
            delete result[0].hashed_password;
            delete result[0].salt;
            delete result[0].token;
            console.log(result[0]);
            res.writeHead(200, {
                'Content-Type': 'application/json'
            });
            res.end(JSON.stringify(result[0]));

        }
    });
});

/*router.get('/profiles/:id/matches', function(res, req, error) {
    //TODO Determine(?) and get User's Matches
});*/

router.post('/profiles/:id/matches', function(res, req, error) {
    //Not used
});

router.get('/profiles/:id/location', function(res, req, error) {
    //TODO: Get User :id's location
});

router.post('/profiles/:id/location', function(res, req, error) {
    //TODO: Update User :id's location
});

router.get('/profiles/:id/averageSpeed', function(res, req, error) {
    //Get User :id's averageSpeed
    console.log("GET: " + JSON.stringify(req.url));
    console.log("param: _ID:" + req.params.id);
    eventsCollection.find({
        _id: mongoDB.helper.toObjectID(req.params.id)
    }).toArray(function(error, result) {
        if (error) {
            next(error);
        } else {
            console.log('Result:');
            console.log(result[0]);
            res.writeHead(200, {
                'Content-Type': 'application/json'
            });
            res.end(JSON.stringify(result[0].averageSpeed));

        }
    });
});

router.post('/profiles/:id/averageSpeed', function(res, req, error) {
    //TODO: Update User :id's averageSpeed
});

router.get('/events', function(req, res, next) {
    eventsCollection.findItems(function(error, result) {
        if (error)
            next(error);
        else {
            console.log(result);
            res.writeHead(200, {
                'Content-Type': 'application/json'
            });

            var obj = {
                results: result
            };
            res.end(JSON.stringify(obj));
        }
    });
});

router.post('/events', function(req, res) {
    //post data to ressource '/events'
    req.body.date = new Date(req.body.date);
    console.log(req.body);
/*    eventsCollection.update({
        id: req.body.id
    }, req.body, {
        upsert: true
    }, function(error) {
        if (error) {
            return console.log('could not update');
        };
        console.log('updated. (upsert: true)');
        res.writeHead(200, 'OK');
        res.end();
    });*/

    eventsCollection.insert(req.body, function(error){
        if (error) {
            next(error);
        }
        res.writeHead(200, 'OK');
        res.end();
    });

    //TODO: ADvertising event. Send notification to possible participants
});

router.get('/events/:id', function(req, res, error) {
    console.log("GET: " + JSON.stringify(req.url));
    console.log("param: _ID:" + req.params.id);
    //var obj_id = BSON.ObjectID.createFromHexString(req.params.id);
    eventsCollection.findOne({_id:mongoDB.helper.toObjectID(req.params.id)},function(error, result){
        if (error){
            next (error);
        }else{
            var event = result;
            console.log('Result:');
            console.log(event);
            console.log(event.start);
            console.log(event.date);
            var daysUntilEvent = daysfromTodayToEventDate(event.date) + 1;
            console.log(daysUntilEvent + " days until event");
            if (daysUntilEvent <= 10 && daysUntilEvent > 0) {
                var options = {
                    host: 'api.wunderground.com',
                    path: '/api/318295f477098775/forecast10day/lang:DE/q/Germany/' + 'Cologne.json'
                };
                var str = '';
                callback = function(response) {
                    //var str = '';

                    //another chunk of data has been recieved, so append it to `str`
                    response.on('data', function(chunk) {
                        str += chunk;
                    });

                    //the whole response has been recieved, so we just print it out here
                    response.on('end', function() {
                        var weatherdata = JSON.parse(str);
                        //console.log(weatherdata.forecast.simpleforecast.forecastday[2]);
                        console.log("High: " + weatherdata.forecast.simpleforecast.forecastday[daysUntilEvent].high["celsius"] +
                            " Low: " + weatherdata.forecast.simpleforecast.forecastday[daysUntilEvent].low["celsius"] +
                            " Conditions: " + weatherdata.forecast.simpleforecast.forecastday[daysUntilEvent].conditions);
                        event['high'] = weatherdata.forecast.simpleforecast.forecastday[daysUntilEvent].high["celsius"];
                        event['low'] = weatherdata.forecast.simpleforecast.forecastday[daysUntilEvent].high["celsius"];
                        event['conditions'] = weatherdata.forecast.simpleforecast.forecastday[daysUntilEvent].conditions;
                        console.log(event);
                        //TODO: Wetterdaten in event integrieren
                    }); 
                };
                http.request(options, callback).end();
            }
            resultsArray = event;
            var jsonObject = {
                results: resultsArray
            };
            res.writeHead(200, {
                'Content-Type': 'application/json'
            });
            res.end(JSON.stringify(jsonObject));
        }
    });
});

router.get('/events/:id/voting/', function(req, res, error) {
    //TODO Get Voting Data
    console.log("GET: " + JSON.stringify(req.url));
    console.log("param: _ID:" + req.params.id);
    eventsCollection.find({
        _id: mongoDB.helper.toObjectID(req.params.id)
    }).toArray(function(error, result) {
        if (error) {
            next(error);
        } else {
            //TODO Return voting data

        }
    });

});
router.post('/events/:id/voting/', function(req, res, error) {
    //TODO submit vote
});

/*
function zum Hinzufügen neuer Teilnehmer
*/
router.put('/events/:id/teilnehmer', function(req, res, error){
    var newparticipant = req.body;
    //console.log("TEST:userID " + req.body.participant_userID + "username: " + req.body.participant_username);
    console.log(JSON.stringify(newparticipant.participant_username));
    eventsCollection.update(
    {_id: mongoDB.helper.toObjectID(req.params.id)},
    {$addToSet : {participants : newparticipant}}, 
    function(error, result){
        if (error) {
            console.log("hi");
            console.log(error);
            next(error);
        }else{
            console.log("success");
            //res.json({response: "Teilnehmer hinzugefügt"});
            res.writeHead(200, 'OK');
            res.end();
        }
    });
});


router.post('/register', function(req,res){
    var username = req.body.username;
    var bdate = req.body.bdate;
    var email = req.body.email;
    var password = req.body.password;
    var gender = req.body.gender;
    var residence = req.body.residence;
    console.log(req.body.bdate + "---" + bdate);
    console.log(req.body.username + "---" + username);
    console.log(req.body.password + "---" + password);
    console.log(req.body.email + "---" + email);
    console.log(req.body.gender + "---" + gender);
    console.log(req.body.residence);
    register.register(username, bdate, email, password, gender, residence, function (found) {
        console.log(found);
        res.json(found);
    });
});


router.post('/login',function(req,res){
    var email = req.body.email;
    var password = req.body.password;
 
    login.login(email,password,function (found) {
        console.log(found);
        res.json(found);
    });
});



function daysBetweenDays(date1, date2) {
    var oneday = 86400000; // 1 Tag in ms

    var date1_ms = date1.getTime();
    var date2_ms = date2.getTime();

    // Differenz berechnen
    var diff_ms = date2_ms - date1_ms;

    // in Tagen konvertieren
    return Math.round(diff_ms / oneday);

}

function daysfromTodayToEventDate(date) {
    var event_date = new Date(date.getFullYear(), date.getMonth(), date.getDate());
    var today = new Date();
    return daysBetweenDays(today, event_date);
}
module.exports = router;
