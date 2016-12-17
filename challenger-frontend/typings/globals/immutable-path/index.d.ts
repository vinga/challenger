


interface ImmuPath {
    map<E,X>(object: E, path: string, provider: (x:X)=>X):E;
    extract(object: any, path: string): any;
    find(object: any, path: string): any;
}
declare module 'immutable-path' {
    var path: ImmuPath;
    export = path;

}




