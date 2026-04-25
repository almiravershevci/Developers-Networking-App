const mongoose = require('mongoose');

const ProjectSchema = new mongoose.Schema({
    userId: { type: String, required: true }, // Kush e postoi
    title: { type: String, required: true },
    description: { type: String, required: true },
    techStack: { type: String }, // Kotlin + Firebase...
    backendStack: { type: String }, // Node.js + Postgres...
    spots: { type: Number, default: 1 }, // Numri i personave
    createdAt: { type: Date, default: Date.now }
});

module.exports = mongoose.model('Project', ProjectSchema);