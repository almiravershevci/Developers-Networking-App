router.get('/data', async (req, res) => {
    try {
        const userId = req.user.id;

        // 1. Numërimet kryesore
        const projectCount = await Project.countDocuments({ userId: userId });
        const openTaskCount = await Task.countDocuments({ userId: userId, status: 'open' });
        const unreadCount = await Message.countDocuments({ receiverId: userId, read: false });
        const matchCount = await MatchRequest.countDocuments({ userId: userId, status: 'pending' });

        // 2. Llogaritjet për subText (Dinamike)

        // Për projekte: sa janë krijuar këtë javë (7 ditët e fundit)
        const oneWeekAgo = new Date(Date.now() - 7 * 24 * 60 * 60 * 1000);
        const newProjectsThisWeek = await Project.countDocuments({ userId: userId, createdAt: { $gte: oneWeekAgo } });

        // Për task-e: sa janë me prioritet të lartë
        const highPriorityCount = await Task.countDocuments({ userId: userId, status: 'open', priority: 'high' });

        // Për mesazhe: sa janë "mentions" (këtu supozojmë që ke një fushë 'type')
        const mentionsCount = await Message.countDocuments({ receiverId: userId, read: false, type: 'mention' });

        // Për match requests: sa janë marrë sot
        const today = new Date();
        today.setHours(0,0,0,0);
        const newMatchesToday = await MatchRequest.countDocuments({ receiverId: userId, status: 'pending', createdAt: { $gte: today } });

        res.json({
            activeProjects: { count: projectCount, subText: `+${newProjectsThisWeek} this week` },
            openTasks: { count: openTaskCount, subText: `${highPriorityCount} high priority` },
            unreadMessages: { count: unreadCount, subText: `${mentionsCount} mentions` },
            matchRequests: { count: matchCount, subText: `+${newMatchesToday} today` }
        });
    } catch (err) {
        res.status(500).json({ error: "Gabim në server!" });
    }
});