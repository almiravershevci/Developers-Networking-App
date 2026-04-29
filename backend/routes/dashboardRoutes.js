const express = require('express');
const router = express.Router();

// Importimi i modeleve të nevojshme
const User = require('../models/User');
const Project = require('../models/Project');
const Task = require('../models/Task');
const Message = require('../models/Message');
const MatchRequest = require('../models/MatchRequest');

// Rruga për marrjen e të dhënave të dashboard-it
router.get('/data', async (req, res) => {
    try {
        // ID-ja e përdoruesit merret nga token-i (përmes middleware-it)
        const userId = req.user.id;

        // 1. Gjej të dhënat e përdoruesit
        const user = await User.findById(userId);
        const userName = user ? user.name : "Përdorues";

        // 2. Definimi i datave për llogaritjet
        const oneWeekAgo = new Date(Date.now() - 7 * 24 * 60 * 60 * 1000);
        const today = new Date();
        today.setHours(0, 0, 0, 0);

        // 3. Ekzekutimi i të gjitha numërimeve paralelisht
        const [
            projectCount,
            openTaskCount,
            unreadCount,
            matchCount,
            newProjectsThisWeek,
            highPriorityCount,
            mentionsCount,
            newMatchesToday
        ] = await Promise.all([
            Project.countDocuments({ userId: userId }),
            Task.countDocuments({ userId: userId, status: 'open' }),
            Message.countDocuments({ receiverId: userId, read: false }),
            MatchRequest.countDocuments({ receiverId: userId, status: 'pending' }),
            Project.countDocuments({ userId: userId, createdAt: { $gte: oneWeekAgo } }),
            Task.countDocuments({ userId: userId, status: 'open', priority: 'high' }),
            Message.countDocuments({ receiverId: userId, read: false, type: 'mention' }),
            MatchRequest.countDocuments({ receiverId: userId, status: 'pending', createdAt: { $gte: today } })
        ]);

        // 4. Kthimi i përgjigjes JSON
        res.json({
            welcomeMessage: `Good evening, ${userName}`,
            activeProjects: {
                count: projectCount,
                subText: `+${newProjectsThisWeek} this week`
            },
            openTasks: {
                count: openTaskCount,
                subText: `${highPriorityCount} high priority`
            },
            unreadMessages: {
                count: unreadCount,
                subText: `${mentionsCount} mentions`
            },
            matchRequests: {
                count: matchCount,
                subText: `+${newMatchesToday} today`
            }
        });

    } catch (err) {
        console.error("Dashboard error:", err);
        res.status(500).json({ error: "Gabim gjatë ngarkimit të dashboard-it." });
    }
});

module.exports = router;