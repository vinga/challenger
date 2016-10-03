

export function hotReloadIfNeeded(store:any, reducerPath:string):any;

declare global {
    interface Date {
        addDays(days: number) : any;
    }
    interface Array<T> {
        contains(obj: T): boolean
    }



}