const express = require('express');
const router = express.Router();
const User = require('../models/User');
const bcrypt = require('bcrypt');

// Rruga për regjistrim
router.post('/register', async (req, res) => {
    try {
        const { name, username, email, password } = req.body;
        const salt = await bcrypt.genSalt(10);
        const hashedPassword = await bcrypt.hash(password, salt);

        const newUser = new User({
            name,
            username,
            email,
            password: hashedPassword
        });

        await newUser.save();
        res.status(201).json({ message: "Përdoruesi u regjistrua me sukses!" });
    } catch (err) {
        res.status(400).json({ error: err.message });
    }
}); // <--- Kjo kllapë i mungonte kodit tënd!

// Rruga për Login
router.post('/login', async (req, res) => {
    try {
        const { email, password } = req.body;
        const user = await User.findOne({ email });

        if (!user) return res.status(400).json({ message: "Përdoruesi nuk ekziston!" });

        const isMatch = await bcrypt.compare(password, user.password);
        if (!isMatch) return res.status(400).json({ message: "Fjalëkalim i gabuar!" });

        res.json({ message: "Mirëseerdhe!", user: { name: user.name } });
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
});

// Rruga për përditësimin e profilit
router.put('/update/:username', async (req, res) => {
    try {
        // Gjejmë përdoruesin dhe përditësojmë fushat që vijnë nga "Edit Profile"
        const updatedUser = await User.findOneAndUpdate(
            { username: req.params.username },
            {
                name: req.body.name,
                title: req.body.title, // Kjo është "Role" në app
                bio: req.body.bio
            },
            { new: true } // Kjo na kthen të dhënat e reja pas ruajtjes
        );

        if (!updatedUser) return res.status(404).json({ message: "Përdoruesi nuk u gjet!" });

        res.json({ message: "Profili u përditësua me sukses!", user: updatedUser });
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
});

module.exports = router;