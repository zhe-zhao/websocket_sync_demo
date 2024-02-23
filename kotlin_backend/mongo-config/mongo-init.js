db.createUser({
    user: "sync_demo_user",
    pwd: "sync_demo_pwd",
    roles: [
        {
            role: "readWrite",
            db: "sync_demo"
        }
    ]
});
