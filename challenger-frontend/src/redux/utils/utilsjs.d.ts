

export function hotReloadIfNeeded(store:any):any;

declare global {
    interface Date {
        addDays(days: number) : any;
        simpleFormat():string,
        simpleFormatWithMinutes():string
    }
    interface Array<T> {
        contains(obj: T): boolean
    }


    const DEVELOPMENT_MODE:any;


}