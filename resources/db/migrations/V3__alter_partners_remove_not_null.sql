ALTER table partners
    ALTER column description DROP not null;

ALTER table partners
    ALTER column email DROP not null;

ALTER table partners
    ALTER column phone DROP not null;