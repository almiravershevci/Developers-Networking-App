const mongoose = require('mongoose');

const userSchema = new mongoose.Schema({
    // Fushat bazë (të detyrueshme)
    name: { type: String, required: true },
    username: { type: String, required: true, unique: true },
    email: { type: String, required: true, unique: true },
    password: { type: String, required: true },

    // Fushat e profilit (të dhënat për UI)
    title: { type: String, default: "Developer" },
    bio: { type: String, default: "" },
    rating: { type: Number, default: 0 },
    projectsCount: { type: Number, default: 0 },
    collaborationsCount: { type: Number, default: 0 },
    messagesCount: { type: Number, default: 0 },
    eventsCount: { type: Number, default: 0 },
    skills: { type: [String], default: [] },

    recentActivity: [{
        action: String,
        timestamp: { type: Date, default: Date.now }
    }],

    links: {
        github: String,
        linkedin: String,
        portfolio: String
    }
});

module.exports = mongoose.model('User', userSchema);