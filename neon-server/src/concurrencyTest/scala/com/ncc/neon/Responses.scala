package com.ncc.neon

object Responses {
    val mongo_all_data = """{"data":[{"_id":"5137b623a9f279d831b6fb86","city":"Arlington","firstname":"Bill","hiredate":"2012-09-15T00:00:00Z","lastname":"Jones","location":{"coordinates":[20,12],"type":"Point"},"salary":100000,"state":"VA"},{"_id":"5137b623a9f279d831b6fb87","city":"Washington","firstname":"Tom","hiredate":"2011-10-17T00:00:00Z","lastname":"Smith","location":{"coordinates":[25,12.1],"type":"Point"},"salary":85000,"state":"DC"},{"_id":"5137b623a9f279d831b6fb88","city":"Washington","firstname":"Maggie","hiredate":"2011-10-15T00:00:00Z","lastname":"Thomas","location":{"coordinates":[19.55,11.92],"type":"Point"},"salary":175000,"state":"DC"},{"_id":"5137b623a9f279d831b6fb89","city":"Alexandria","firstname":"Andrew","hiredate":"2013-01-10T00:00:00Z","lastname":"Wilks","location":{"coordinates":[10,7],"type":"Point"},"salary":55000,"state":"VA"},{"_id":"5137b623a9f279d831b6fb8a","city":"Arlington","firstname":"Michelle","hiredate":"2010-06-15T00:00:00Z","lastname":"Jackson","location":{"coordinates":[12,11],"type":"Point"},"salary":118000,"state":"VA"},{"_id":"5137b623a9f279d831b6fb8b","city":"Washington","firstname":"Rodney","hiredate":"2012-10-30T00:00:00Z","location":{"coordinates":[14,22],"type":"Point"},"salary":88000,"state":"DC"},{"_id":"5137b623a9f279d831b6fb8c","city":"Germantown","firstname":"Anthony","hiredate":"2012-02-15T00:00:00Z","lastname":"Bowles","location":{"coordinates":[11,-23],"type":"Point"},"salary":105000,"state":"MD"},{"_id":"5137b623a9f279d831b6fb8d","city":"Arlington","firstname":"Fred","hiredate":"2012-02-21T00:00:00Z","lastname":"Porter","location":{"coordinates":[20,25],"type":"Point"},"salary":60000,"state":"VA"}]}"""

    val mongo_filtered_data = """{"data":[{"_id":"5137b623a9f279d831b6fb86","city":"Arlington","firstname":"Bill","hiredate":"2012-09-15T00:00:00Z","lastname":"Jones","location":{"coordinates":[20,12],"type":"Point"},"salary":100000,"state":"VA"},{"_id":"5137b623a9f279d831b6fb87","city":"Washington","firstname":"Tom","hiredate":"2011-10-17T00:00:00Z","lastname":"Smith","location":{"coordinates":[25,12.1],"type":"Point"},"salary":85000,"state":"DC"},{"_id":"5137b623a9f279d831b6fb88","city":"Washington","firstname":"Maggie","hiredate":"2011-10-15T00:00:00Z","lastname":"Thomas","location":{"coordinates":[19.55,11.92],"type":"Point"},"salary":175000,"state":"DC"},{"_id":"5137b623a9f279d831b6fb89","city":"Alexandria","firstname":"Andrew","hiredate":"2013-01-10T00:00:00Z","lastname":"Wilks","location":{"coordinates":[10,7],"type":"Point"},"salary":55000,"state":"VA"},{"_id":"5137b623a9f279d831b6fb8a","city":"Arlington","firstname":"Michelle","hiredate":"2010-06-15T00:00:00Z","lastname":"Jackson","location":{"coordinates":[12,11],"type":"Point"},"salary":118000,"state":"VA"},{"_id":"5137b623a9f279d831b6fb8b","city":"Washington","firstname":"Rodney","hiredate":"2012-10-30T00:00:00Z","location":{"coordinates":[14,22],"type":"Point"},"salary":88000,"state":"DC"},{"_id":"5137b623a9f279d831b6fb8d","city":"Arlington","firstname":"Fred","hiredate":"2012-02-21T00:00:00Z","lastname":"Porter","location":{"coordinates":[20,25],"type":"Point"},"salary":60000,"state":"VA"}]}"""

    val mongo_selection_data = """{"data":[{"_id":"5137b623a9f279d831b6fb86","city":"Arlington","firstname":"Bill","hiredate":"2012-09-15T00:00:00Z","lastname":"Jones","location":{"coordinates":[20,12],"type":"Point"},"salary":100000,"state":"VA"},{"_id":"5137b623a9f279d831b6fb87","city":"Washington","firstname":"Tom","hiredate":"2011-10-17T00:00:00Z","lastname":"Smith","location":{"coordinates":[25,12.1],"type":"Point"},"salary":85000,"state":"DC"},{"_id":"5137b623a9f279d831b6fb89","city":"Alexandria","firstname":"Andrew","hiredate":"2013-01-10T00:00:00Z","lastname":"Wilks","location":{"coordinates":[10,7],"type":"Point"},"salary":55000,"state":"VA"},{"_id":"5137b623a9f279d831b6fb8b","city":"Washington","firstname":"Rodney","hiredate":"2012-10-30T00:00:00Z","location":{"coordinates":[14,22],"type":"Point"},"salary":88000,"state":"DC"},{"_id":"5137b623a9f279d831b6fb8d","city":"Arlington","firstname":"Fred","hiredate":"2012-02-21T00:00:00Z","lastname":"Porter","location":{"coordinates":[20,25],"type":"Point"},"salary":60000,"state":"VA"}]}"""

    val hive_all_data = """{"data":[{"hiredate":"2012-09-15T00:00:00Z","_id":"5137b623a9f279d831b6fb86","state":"VA","lastname":"Jones","firstname":"Bill","salary":100000,"city":"Arlington"},{"hiredate":"2011-10-17T00:00:00Z","_id":"5137b623a9f279d831b6fb87","state":"DC","lastname":"Smith","firstname":"Tom","salary":85000,"city":"Washington"},{"hiredate":"2011-10-15T00:00:00Z","_id":"5137b623a9f279d831b6fb88","state":"DC","lastname":"Thomas","firstname":"Maggie","salary":175000,"city":"Washington"},{"hiredate":"2013-01-10T00:00:00Z","_id":"5137b623a9f279d831b6fb89","state":"VA","lastname":"Wilks","firstname":"Andrew","salary":55000,"city":"Alexandria"},{"hiredate":"2010-06-15T00:00:00Z","_id":"5137b623a9f279d831b6fb8a","state":"VA","lastname":"Jackson","firstname":"Michelle","salary":118000,"city":"Arlington"},{"hiredate":"2012-10-30T00:00:00Z","_id":"5137b623a9f279d831b6fb8b","state":"DC","lastname":null,"firstname":"Rodney","salary":88000,"city":"Washington"},{"hiredate":"2012-02-15T00:00:00Z","_id":"5137b623a9f279d831b6fb8c","state":"MD","lastname":"Bowles","firstname":"Anthony","salary":105000,"city":"Germantown"},{"hiredate":"2012-02-21T00:00:00Z","_id":"5137b623a9f279d831b6fb8d","state":"VA","lastname":"Porter","firstname":"Fred","salary":60000,"city":"Arlington"}]}"""

    val hive_filtered_data = """{"data":[{"hiredate":"2012-09-15T00:00:00Z","_id":"5137b623a9f279d831b6fb86","state":"VA","lastname":"Jones","firstname":"Bill","salary":100000,"city":"Arlington"},{"hiredate":"2011-10-17T00:00:00Z","_id":"5137b623a9f279d831b6fb87","state":"DC","lastname":"Smith","firstname":"Tom","salary":85000,"city":"Washington"},{"hiredate":"2011-10-15T00:00:00Z","_id":"5137b623a9f279d831b6fb88","state":"DC","lastname":"Thomas","firstname":"Maggie","salary":175000,"city":"Washington"},{"hiredate":"2013-01-10T00:00:00Z","_id":"5137b623a9f279d831b6fb89","state":"VA","lastname":"Wilks","firstname":"Andrew","salary":55000,"city":"Alexandria"},{"hiredate":"2010-06-15T00:00:00Z","_id":"5137b623a9f279d831b6fb8a","state":"VA","lastname":"Jackson","firstname":"Michelle","salary":118000,"city":"Arlington"},{"hiredate":"2012-10-30T00:00:00Z","_id":"5137b623a9f279d831b6fb8b","state":"DC","lastname":null,"firstname":"Rodney","salary":88000,"city":"Washington"},{"hiredate":"2012-02-21T00:00:00Z","_id":"5137b623a9f279d831b6fb8d","state":"VA","lastname":"Porter","firstname":"Fred","salary":60000,"city":"Arlington"}]}"""

    val hive_selection_data = """{"data":[{"hiredate":"2012-09-15T00:00:00Z","_id":"5137b623a9f279d831b6fb86","state":"VA","lastname":"Jones","firstname":"Bill","salary":100000,"city":"Arlington"},{"hiredate":"2011-10-17T00:00:00Z","_id":"5137b623a9f279d831b6fb87","state":"DC","lastname":"Smith","firstname":"Tom","salary":85000,"city":"Washington"},{"hiredate":"2013-01-10T00:00:00Z","_id":"5137b623a9f279d831b6fb89","state":"VA","lastname":"Wilks","firstname":"Andrew","salary":55000,"city":"Alexandria"},{"hiredate":"2012-10-30T00:00:00Z","_id":"5137b623a9f279d831b6fb8b","state":"DC","lastname":null,"firstname":"Rodney","salary":88000,"city":"Washington"},{"hiredate":"2012-02-21T00:00:00Z","_id":"5137b623a9f279d831b6fb8d","state":"VA","lastname":"Porter","firstname":"Fred","salary":60000,"city":"Arlington"}]}"""
}