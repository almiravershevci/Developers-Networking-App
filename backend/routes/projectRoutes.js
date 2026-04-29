const express = require('express');
const router = express.Router();
const Project = require('../models/Project');

// Krijimi i një postimi të ri
router.post('/create', async (req, res) => {
    try {
        const newProject = new Project(req.body);
        const savedProject = await newProject.save();
        res.status(201).json(savedProject);
    } catch (err) {
        res.status(500).json({ error: "Gabim gjatë postimit!" });
    }
});

// Marrja e të gjitha postimeve për t'i shfaqur në Feed
router.get('/all', async (req, res) => {
    try {
        const projects = await Project.find().sort({ createdAt: -1 });
        res.json(projects);
    } catch (err) {
        res.status(500).json({ error: "Nuk mund të ngarkoheshin projektet." });
    }
});

module.exports = router;